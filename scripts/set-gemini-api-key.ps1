# ===========================================
# Safety Road - Gemini API Key 환경변수 설정
# ===========================================
# 이 스크립트는 현재 세션에만 환경변수를 설정합니다.
# 영구적으로 설정하려면 시스템 환경변수에 직접 추가하거나
# [System.Environment]::SetEnvironmentVariable()을 사용하세요.
#
# 주의: API Key는 소스코드에 포함하지 마세요!
# Google AI Studio에서 발급받은 키를 사용하세요:
# https://aistudio.google.com/app/apikey

if ([string]::IsNullOrEmpty($env:GEMINI_API_KEY)) {
    Write-Host "GEMINI_API_KEY 환경변수가 설정되어 있지 않습니다." -ForegroundColor Red
    Write-Host ""
    Write-Host "다음 명령으로 환경변수를 설정하세요:" -ForegroundColor Cyan
    Write-Host '  $env:GEMINI_API_KEY = "YOUR_API_KEY_HERE"' -ForegroundColor White
    Write-Host ""
    Write-Host "또는 영구적으로 설정하려면:" -ForegroundColor Cyan
    Write-Host '  [System.Environment]::SetEnvironmentVariable("GEMINI_API_KEY", "YOUR_API_KEY_HERE", "User")' -ForegroundColor White
    Write-Host ""
    Write-Host "API Key는 https://aistudio.google.com/app/apikey 에서 발급받으세요." -ForegroundColor Yellow
} else {
    Write-Host "GEMINI_API_KEY 환경변수가 이미 설정되어 있습니다." -ForegroundColor Green
    Write-Host "현재 키: $($env:GEMINI_API_KEY.Substring(0,10))..." -ForegroundColor Gray
}
Write-Host ""

