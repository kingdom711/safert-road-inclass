# ===========================================
# Safety Road - Gemini API Key 영구 환경변수 설정
# ===========================================
# 이 스크립트는 사용자 환경변수에 영구적으로 설정합니다.

$apiKey = "AIzaSyApVKXwo46wo1kPa4mnRneDMxPZImb0TcE"

try {
    [System.Environment]::SetEnvironmentVariable("GEMINI_API_KEY", $apiKey, "User")
    Write-Host "GEMINI_API_KEY 환경변수가 영구적으로 설정되었습니다." -ForegroundColor Green
    Write-Host "새로운 터미널 세션에서 적용됩니다." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "현재 세션에도 적용하려면 다음 명령을 실행하세요:" -ForegroundColor Cyan
    Write-Host "`$env:GEMINI_API_KEY = `"$apiKey`"" -ForegroundColor White
    Write-Host ""
} catch {
    Write-Host "환경변수 설정 중 오류가 발생했습니다: $_" -ForegroundColor Red
    Write-Host "관리자 권한으로 실행해보세요." -ForegroundColor Yellow
}

