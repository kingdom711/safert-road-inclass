@echo off
echo ==========================================
echo 🚀 Starting Frontend Server (Vite)...
echo ==========================================

cd frontend
if not exist node_modules (
    echo 📦 Installing dependencies...
    call npm install
)

call npm run dev
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Frontend start failed!
    pause
    exit /b %ERRORLEVEL%
)
pause

