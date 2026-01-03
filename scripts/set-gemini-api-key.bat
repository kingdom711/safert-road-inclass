@echo off
REM ===========================================
REM Safety Road - Gemini API Key 환경변수 설정
REM ===========================================
REM 이 스크립트는 현재 세션에만 환경변수를 설정합니다.
REM 영구적으로 설정하려면 시스템 환경변수에 직접 추가하거나
REM setx 명령을 사용하세요.

set GEMINI_API_KEY=AIzaSyApVKXwo46wo1kPa4mnRneDMxPZImb0TcE

echo GEMINI_API_KEY 환경변수가 설정되었습니다.
echo 현재 세션에서만 유효합니다.
echo.
echo 영구적으로 설정하려면 다음 명령을 실행하세요:
echo setx GEMINI_API_KEY "AIzaSyApVKXwo46wo1kPa4mnRneDMxPZImb0TcE"
echo.
echo 또는 Windows 시스템 환경변수 설정에서 직접 추가하세요.
echo.

pause

