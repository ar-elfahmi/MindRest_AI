# verify-gemini.ps1
# Verifikasi GEMINI_API_KEY di Supabase Edge Function secrets
# via function `test-gemini` yang sudah ada.
#
# Cara run (dari PowerShell, di folder mana saja):
#   powershell -ExecutionPolicy Bypass -File "C:\laragon\www\MindRest_AI\verify-gemini.ps1"

$ErrorActionPreference = "Stop"

# --- 1. Path absolut ke .env (tidak peduli cwd) -----------------------
$envPath = "C:\laragon\www\MindRest_AI\.env"
if (-not (Test-Path $envPath)) {
    Write-Host "[ERROR] .env not found at: $envPath" -ForegroundColor Red
    Write-Host "        Pastikan file ada. Cek: Test-Path '$envPath'" -ForegroundColor Yellow
    exit 1
}

# --- 2. Baca .env -----------------------------------------------------
$lines = Get-Content $envPath
$anonKey = ($lines | Where-Object { $_ -match "^SUPABASE_ANON_KEY=" }) -replace "^SUPABASE_ANON_KEY=", ""
$supaUrl = ($lines | Where-Object { $_ -match "^SUPABASE_URL=" }) -replace "^SUPABASE_URL=", ""

if ([string]::IsNullOrWhiteSpace($anonKey) -or [string]::IsNullOrWhiteSpace($supaUrl)) {
    Write-Host "[ERROR] SUPABASE_ANON_KEY atau SUPABASE_URL kosong di .env" -ForegroundColor Red
    Write-Host "        Cek isi: Get-Content $envPath" -ForegroundColor Yellow
    exit 1
}

# Extract project ref dari URL (twaphoalrrgujsnhpez.supabase.co -> twaphoalrrgujsnhpez)
$projRef = ($supaUrl -replace "https://", "").Split(".")[0]
$url = "https://$projRef.supabase.co/functions/v1/test-gemini"

Write-Host ""
Write-Host "Project ref  : $projRef" -ForegroundColor Cyan
Write-Host "Anon key     : $($anonKey.Substring(0,30))..." -ForegroundColor Cyan
Write-Host "Function URL : $url" -ForegroundColor Cyan
Write-Host ""

# --- 3. Helper untuk invoke + format response -----------------------
function Invoke-TestGemini {
    param([string]$Method = "GET", [string]$Body = "")

    Write-Host ""
    Write-Host "--- Testing $Method $(if ($Body) {'(custom prompt)'}) ---" -ForegroundColor Yellow

    $headers = @{ "Authorization" = "Bearer $anonKey" }
    if ($Body) { $headers["Content-Type"] = "application/json" }

    $params = @{
        Uri         = $url
        Method      = $Method
        Headers     = $headers
        UseBasicParsing = $true
    }
    if ($Body) { $params["Body"] = $Body }

    try {
        $res = Invoke-WebRequest @params -TimeoutSec 30
        Write-Host "[HTTP $($res.StatusCode)]" -ForegroundColor Green
        Write-Host $res.Content
    } catch {
        $code = "?"
        if ($_.Exception.Response) {
            $code = [int]$_.Exception.Response.StatusCode
        }
        Write-Host "[HTTP $code]" -ForegroundColor Red
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = [System.IO.StreamReader]::new($stream)
                Write-Host "Body:" -ForegroundColor Red
                Write-Host $reader.ReadToEnd() -ForegroundColor Red
            } catch {}
        }
    }
}

# --- 4. Run tests -----------------------------------------------------
Invoke-TestGemini -Method "GET"
Invoke-TestGemini -Method "POST" -Body '{"prompt":"Sebut 3 warna primer dalam 1 kalimat."}'

# --- 5. Footer -------------------------------------------------------
Write-Host ""
Write-Host "=== END ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "PANDUAN BACA OUTPUT:" -ForegroundColor Cyan
Write-Host "  [HTTP 200] + ok:true  -> GEMINI_API_KEY BENAR, lanjut T-003"
Write-Host "  [HTTP 500] + error:GEMINI_API_KEY not configured -> secret belum masuk Supabase"
Write-Host "  [HTTP 401/403]         -> anon key salah / function butuh JWT user"
Write-Host "  [HTTP 404]             -> project ref salah / function belum di-deploy"
Write-Host "  network error          -> cek koneksi internet"
Write-Host ""
