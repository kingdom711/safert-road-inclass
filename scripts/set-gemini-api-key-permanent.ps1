# ===========================================
# Safety Road - Gemini API Key 영구 환경변수 설정
# ===========================================
# 이 스크립트는 사용자 환경변수에 영구적으로 설정합니다.
#
# 주의: API Key는 소스코드에 포함하지 마세요!
# Google AI Studio에서 발급받은 키를 사용하세요:
# https://aistudio.google.com/app/apikey

Write-Host "===========================================" -ForegroundColor Cyan
Write-Host " Gemini API Key 영구 환경변수 설정" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "API Key는 https://aistudio.google.com/app/apikey 에서 발급받으세요." -ForegroundColor Yellow
Write-Host ""

$apiKey = Read-Host "Gemini API Key를 입력하세요"

if ([string]::IsNullOrEmpty($apiKey)) {
    Write-Host "API Key가 입력되지 않았습니다." -ForegroundColor Red
    exit 1
}

try {
    [System.Environment]::SetEnvironmentVariable("GEMINI_API_KEY", $apiKey, "User")
    $env:GEMINI_API_KEY = $apiKey
    Write-Host ""
    Write-Host "GEMINI_API_KEY 환경변수가 영구적으로 설정되었습니다." -ForegroundColor Green
    Write-Host "새로운 터미널 세션에서 적용됩니다." -ForegroundColor Yellow
    Write-Host "현재 세션에도 자동 적용되었습니다." -ForegroundColor Green
    Write-Host ""
}
catch {
    Write-Host "환경변수 설정 중 오류가 발생했습니다: $_" -ForegroundColor Red
    Write-Host "관리자 권한으로 실행해보세요." -ForegroundColor Yellow
}

