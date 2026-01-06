@echo off
REM ===========================================
REM Safety Road - Gemini API Key 영구 환경변수 설정
REM ===========================================
REM 이 스크립트는 사용자 환경변수에 영구적으로 설정합니다.
REM 관리자 권한이 필요할 수 있습니다.
REM
REM 주의: API Key는 소스코드에 포함하지 마세요!
REM Google AI Studio에서 발급받은 키를 사용하세요:
REM https://aistudio.google.com/app/apikey

echo ===========================================
echo  Gemini API Key 영구 환경변수 설정
echo ===========================================
echo.
echo API Key는 https://aistudio.google.com/app/apikey 에서 발급받으세요.
echo.

set /p API_KEY="Gemini API Key를 입력하세요: "

if "%API_KEY%"=="" (
    echo API Key가 입력되지 않았습니다.
    pause
    exit /b 1
)

setx GEMINI_API_KEY "%API_KEY%"

echo.
echo GEMINI_API_KEY 환경변수가 영구적으로 설정되었습니다.
echo 새로운 터미널 세션에서 적용됩니다.
echo.
echo 현재 세션에도 적용하려면 다음 명령을 실행하세요:
echo   set GEMINI_API_KEY=%API_KEY%
echo.
pause

