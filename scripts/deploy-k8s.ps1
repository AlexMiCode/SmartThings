param(
    [string]$Namespace = "smartthings"
)

$ErrorActionPreference = "Stop"

kubectl apply -f .\k8s\01-namespace.yaml
kubectl apply -f .\k8s\02-config.yaml
kubectl apply -f .\k8s\03-data.yaml
kubectl apply -f .\k8s\04-platform.yaml
kubectl apply -f .\k8s\05-monitoring-ingress.yaml

kubectl rollout status deployment/config-server -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/discovery-server -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/user-service -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/product-service -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/order-service -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/notification-service -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/api-gateway -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/frontend -n $Namespace --timeout=240s | Out-Null
kubectl rollout status deployment/prometheus -n $Namespace --timeout=240s | Out-Null

Write-Host "Kubernetes deploy completed"
