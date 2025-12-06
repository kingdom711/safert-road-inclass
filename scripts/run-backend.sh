#!/bin/bash

echo "=========================================="
echo "🚀 Starting Backend Server (Spring Boot)..."
echo "=========================================="

cd backend || { echo "❌ 'backend' directory not found"; exit 1; }

# Grant execution permission just in case
chmod +x gradlew

# Run Spring Boot
./gradlew bootRun

