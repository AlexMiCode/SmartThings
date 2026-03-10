$ErrorActionPreference = "Stop"

$urls = @(
    "http://localhost:8888/actuator/health",
    "http://localhost:8761/actuator/health",
    "http://localhost:8091/actuator/health",
    "http://localhost:8092/actuator/health",
    "http://localhost:8093/actuator/health",
    "http://localhost:8094/actuator/health",
    "http://localhost:8080/actuator/health"
)

foreach ($url in $urls) {
    $ok = $false
    for ($i = 0; $i -lt 20; $i++) {
        try {
            $response = Invoke-RestMethod -Uri $url -TimeoutSec 5
            if ($response.status -eq "UP") {
                $ok = $true
                Write-Output "$url => UP"
                break
            }
        } catch {
            Start-Sleep -Seconds 3
        }
    }

    if (-not $ok) {
        throw "Health check failed for $url"
    }
}

$products = $null
for ($i = 0; $i -lt 20; $i++) {
    try {
        $products = Invoke-RestMethod -Uri "http://localhost:8080/api/products" -TimeoutSec 10
        if ($products -and $products.Count -ge 1) {
            Write-Output "Gateway catalog smoke test passed"
            break
        }
    } catch {
        Start-Sleep -Seconds 3
    }
}

if (-not $products -or $products.Count -lt 1) {
    throw "Catalog smoke test failed: no products returned through gateway"
}
