#!/bin/bash

echo "=========================================="
echo "🏗️  Building Project (Backend + Frontend)..."
echo "=========================================="

# 1. Backend Build
echo "Step 1: Building Backend..."
cd backend || { echo "❌ 'backend' directory not found"; exit 1; }
chmod +x gradlew
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "❌ Backend build failed!"
    exit 1
fi
echo "✅ Backend build success!"
cd ..

# 2. Frontend Build
echo "Step 2: Building Frontend..."
cd frontend || { echo "❌ 'frontend' directory not found"; exit 1; }
npm install
npm run build
if [ $? -ne 0 ]; then
    echo "❌ Frontend build failed!"
    exit 1
fi
echo "✅ Frontend build success!"
cd ..

echo "=========================================="
echo "🎉 All builds completed successfully!"
echo "=========================================="

