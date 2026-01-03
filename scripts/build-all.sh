#!/bin/bash

echo "=========================================="
echo "🏗️  Building Backend Project..."
echo "=========================================="

# Backend Build
echo "Building Backend..."
cd backend || { echo "❌ 'backend' directory not found"; exit 1; }
chmod +x gradlew
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "❌ Backend build failed!"
    exit 1
fi
echo "✅ Backend build success!"
cd ..

echo "=========================================="
echo "🎉 Build completed successfully!"
echo "=========================================="

