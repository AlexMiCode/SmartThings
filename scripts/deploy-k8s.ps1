param(
    [string]$Namespace = "smartthings"
)

$ErrorActionPreference = "Stop"

function Invoke-Kubectl {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    & kubectl @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "kubectl $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Test-KubectlContext {
    & kubectl config current-context | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Kubernetes context is not configured. Start a cluster and select a context before deployment."
    }

    & kubectl cluster-info | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "kubectl cannot connect to the current cluster. Check that your Kubernetes cluster is running."
    }
}

Test-KubectlContext

Invoke-Kubectl -Arguments @("apply", "-f", ".\k8s\01-namespace.yaml")
Invoke-Kubectl -Arguments @("apply", "-f", ".\k8s\02-config.yaml")
Invoke-Kubectl -Arguments @("apply", "-f", ".\k8s\03-data.yaml")
Invoke-Kubectl -Arguments @("apply", "-f", ".\k8s\04-platform.yaml")
Invoke-Kubectl -Arguments @("apply", "-f", ".\k8s\05-monitoring-ingress.yaml")

Invoke-Kubectl -Arguments @("rollout", "status", "deployment/config-server", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/discovery-server", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/user-service", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/product-service", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/order-service", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/notification-service", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/api-gateway", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/frontend", "-n", $Namespace, "--timeout=240s")
Invoke-Kubectl -Arguments @("rollout", "status", "deployment/prometheus", "-n", $Namespace, "--timeout=240s")

Write-Host "Kubernetes deploy completed"
