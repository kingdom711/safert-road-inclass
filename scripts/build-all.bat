@echo off
echo ==========================================
echo 🏗️  Building Backend Project...
echo ==========================================

echo Building Backend...
cd backend
call gradlew.bat clean build -x test
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Backend build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo ✅ Backend build success!
cd ..

echo ==========================================
echo 🎉 Build completed successfully!
echo ==========================================
pause

