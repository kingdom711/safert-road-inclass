@echo off
echo ==========================================
echo 🏗️  Building Project (Backend + Frontend)...
echo ==========================================

echo Step 1: Building Backend...
cd backend
call gradlew.bat clean build -x test
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Backend build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo ✅ Backend build success!
cd ..

echo Step 2: Building Frontend...
cd frontend
call npm install
call npm run build
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Frontend build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo ✅ Frontend build success!
cd ..

echo ==========================================
echo 🎉 All builds completed successfully!
echo ==========================================
pause

