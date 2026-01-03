@echo off
REM ===========================================
REM Safety Road - Gemini API Key 영구 환경변수 설정
REM ===========================================
REM 이 스크립트는 사용자 환경변수에 영구적으로 설정합니다.
REM 관리자 권한이 필요할 수 있습니다.

setx GEMINI_API_KEY "AIzaSyApVKXwo46wo1kPa4mnRneDMxPZImb0TcE"

echo.
echo GEMINI_API_KEY 환경변수가 영구적으로 설정되었습니다.
echo 새로운 터미널 세션에서 적용됩니다.
echo.
echo 현재 세션에도 적용하려면 다음 명령을 실행하세요:
echo set GEMINI_API_KEY=AIzaSyApVKXwo46wo1kPa4mnRneDMxPZImb0TcE
echo.
pause

