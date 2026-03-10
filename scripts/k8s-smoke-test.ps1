param(
    [string]$Namespace = "smartthings"
)

$ErrorActionPreference = "Stop"

function Invoke-Kubectl {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $output = & kubectl @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "kubectl $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
    return $output
}

function Test-KubectlContext {
    & kubectl config current-context | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Kubernetes context is not configured. Start a cluster and select a context before smoke testing."
    }

    & kubectl cluster-info | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "kubectl cannot connect to the current cluster. Check that your Kubernetes cluster is running."
    }
}

function Wait-Deployment {
    param([string]$Name)
    Invoke-Kubectl -Arguments @("rollout", "status", "deployment/$Name", "-n", $Namespace, "--timeout=240s") | Out-Null
}

function Get-ReadyReplicaCount {
    param([string]$Name)
    $json = Invoke-Kubectl -Arguments @("get", "deployment", $Name, "-n", $Namespace, "-o", "json") | ConvertFrom-Json
    if ($null -eq $json.status.readyReplicas) {
        return 0
    }
    return [int]$json.status.readyReplicas
}

function Wait-ReadyReplicas {
    param(
        [string]$Name,
        [int]$Expected,
        [int]$TimeoutSeconds = 240
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $ready = Get-ReadyReplicaCount -Name $Name
        if ($ready -ge $Expected) {
            return
        }
        Start-Sleep -Seconds 5
    } while ((Get-Date) -lt $deadline)

    throw "$Name did not reach $Expected ready replicas within $TimeoutSeconds seconds"
}

Test-KubectlContext

$deployments = @(
    "config-server",
    "discovery-server",
    "user-service",
    "product-service",
    "order-service",
    "notification-service",
    "api-gateway",
    "frontend",
    "prometheus"
)

foreach ($deployment in $deployments) {
    Wait-Deployment -Name $deployment
}

$hpaBackupPath = Join-Path $env:TEMP "product-service-hpa-backup.yaml"
Invoke-Kubectl -Arguments @("get", "hpa", "product-service-hpa", "-n", $Namespace, "-o", "yaml") | Set-Content -Path $hpaBackupPath -Encoding utf8

$initialPod = Invoke-Kubectl -Arguments @("get", "pods", "-n", $Namespace, "-l", "app=product-service", "-o", "jsonpath={.items[0].metadata.name}")
Invoke-Kubectl -Arguments @("delete", "pod", $initialPod, "-n", $Namespace, "--wait=false") | Out-Null
Start-Sleep -Seconds 5
Wait-Deployment -Name "product-service"

Invoke-Kubectl -Arguments @("delete", "hpa", "product-service-hpa", "-n", $Namespace) | Out-Null
Invoke-Kubectl -Arguments @("scale", "deployment", "product-service", "-n", $Namespace, "--replicas=3") | Out-Null
Wait-Deployment -Name "product-service"
Wait-ReadyReplicas -Name "product-service" -Expected 3

Invoke-Kubectl -Arguments @("rollout", "restart", "deployment/product-service", "-n", $Namespace) | Out-Null
Wait-Deployment -Name "product-service"
Wait-ReadyReplicas -Name "product-service" -Expected 3

Invoke-Kubectl -Arguments @("apply", "-f", $hpaBackupPath) | Out-Null
Invoke-Kubectl -Arguments @("scale", "deployment", "product-service", "-n", $Namespace, "--replicas=2") | Out-Null
Wait-Deployment -Name "product-service"
Wait-ReadyReplicas -Name "product-service" -Expected 2

$hpa = Invoke-Kubectl -Arguments @("get", "hpa", "product-service-hpa", "-n", $Namespace, "--ignore-not-found")
if (-not $hpa) {
    throw "product-service-hpa was not found"
}

Write-Host "Kubernetes smoke test passed"
