@echo off
REM ===========================================
REM Safety Road - Gemini API Key 환경변수 설정
REM ===========================================
REM 이 스크립트는 현재 세션에만 환경변수를 설정합니다.
REM 영구적으로 설정하려면 시스템 환경변수에 직접 추가하거나
REM setx 명령을 사용하세요.
REM
REM 주의: API Key는 소스코드에 포함하지 마세요!
REM Google AI Studio에서 발급받은 키를 사용하세요:
REM https://aistudio.google.com/app/apikey

if "%GEMINI_API_KEY%"=="" (
    echo GEMINI_API_KEY 환경변수가 설정되어 있지 않습니다.
    echo.
    echo 다음 명령으로 환경변수를 설정하세요:
    echo   set GEMINI_API_KEY=YOUR_API_KEY_HERE
    echo.
    echo 또는 영구적으로 설정하려면:
    echo   setx GEMINI_API_KEY "YOUR_API_KEY_HERE"
    echo.
    echo API Key는 https://aistudio.google.com/app/apikey 에서 발급받으세요.
) else (
    echo GEMINI_API_KEY 환경변수가 이미 설정되어 있습니다.
    echo 현재 키: %GEMINI_API_KEY:~0,10%...
)
echo.

pause

