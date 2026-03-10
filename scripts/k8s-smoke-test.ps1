param(
    [string]$Namespace = "smartthings"
)

$ErrorActionPreference = "Stop"

function Wait-Deployment {
    param([string]$Name)
    kubectl rollout status deployment/$Name -n $Namespace --timeout=240s | Out-Null
}

function Get-ReadyReplicaCount {
    param([string]$Name)
    $json = kubectl get deployment $Name -n $Namespace -o json | ConvertFrom-Json
    if ($null -eq $json.status.readyReplicas) {
        return 0
    }
    return [int]$json.status.readyReplicas
}

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

$initialPod = kubectl get pods -n $Namespace -l app=product-service -o jsonpath='{.items[0].metadata.name}'
kubectl delete pod $initialPod -n $Namespace --wait=false | Out-Null
Start-Sleep -Seconds 5
Wait-Deployment -Name "product-service"

kubectl scale deployment product-service -n $Namespace --replicas=3 | Out-Null
Wait-Deployment -Name "product-service"
if ((Get-ReadyReplicaCount -Name "product-service") -lt 3) {
    throw "product-service did not scale to 3 replicas"
}

kubectl rollout restart deployment/product-service -n $Namespace | Out-Null
Wait-Deployment -Name "product-service"

kubectl scale deployment product-service -n $Namespace --replicas=2 | Out-Null
Wait-Deployment -Name "product-service"

$hpa = kubectl get hpa product-service-hpa -n $Namespace --ignore-not-found
if (-not $hpa) {
    throw "product-service-hpa was not found"
}

Write-Host "Kubernetes smoke test passed"
