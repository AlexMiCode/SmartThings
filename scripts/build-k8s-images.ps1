param(
    [string]$MinikubeProfile = "minikube"
)

$ErrorActionPreference = "Stop"

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)]
        [string]$FilePath,
        [string[]]$Arguments = @()
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$FilePath $($Arguments -join ' ') failed with exit code $LASTEXITCODE"
    }
}

function Test-Minikube {
    & minikube -p $MinikubeProfile status | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Minikube profile '$MinikubeProfile' is not running."
    }
}

Test-Minikube

$images = @(
    @{
        Name = "config-server"
        Tag = "smartthings/config-server:latest"
        Context = "."
        Dockerfile = "config-server/Dockerfile"
        BuildArgs = @()
    },
    @{
        Name = "discovery-server"
        Tag = "smartthings/discovery-server:latest"
        Context = "."
        Dockerfile = "discovery-server/Dockerfile"
        BuildArgs = @()
    },
    @{
        Name = "user-service"
        Tag = "smartthings/user-service:latest"
        Context = "."
        Dockerfile = "user-service/Dockerfile"
        BuildArgs = @()
    },
    @{
        Name = "product-service"
        Tag = "smartthings/product-service:latest"
        Context = "."
        Dockerfile = "product-service/Dockerfile"
        BuildArgs = @()
    },
    @{
        Name = "order-service"
        Tag = "smartthings/order-service:latest"
        Context = "."
        Dockerfile = "order-service/Dockerfile"
        BuildArgs = @()
    },
    @{
        Name = "notification-service"
        Tag = "smartthings/notification-service:latest"
        Context = "."
        Dockerfile = "notification-service/Dockerfile"
        BuildArgs = @()
    },
    @{
        Name = "api-gateway"
        Tag = "smartthings/api-gateway:latest"
        Context = "."
        Dockerfile = "api-gateway/Dockerfile"
        BuildArgs = @()
    },
    @{
        Name = "frontend"
        Tag = "smartthings/frontend:latest"
        Context = ".\\frontend"
        Dockerfile = "frontend/Dockerfile"
        BuildArgs = @("--build-arg", "VITE_API_URL=http://smartthings.local/api")
    }
)

foreach ($image in $images) {
    Write-Host "Building $($image.Tag)"
    $dockerArgs = @(
        "build",
        "-t", $image.Tag,
        "-f", $image.Dockerfile
    ) + $image.BuildArgs + @($image.Context)
    Invoke-Checked -FilePath "docker" -Arguments $dockerArgs

    Write-Host "Loading $($image.Tag) into minikube"
    Invoke-Checked -FilePath "minikube" -Arguments @("-p", $MinikubeProfile, "image", "load", $image.Tag)
}

Write-Host "Minikube images are ready"
