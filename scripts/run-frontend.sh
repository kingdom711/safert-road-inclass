#!/bin/bash

echo "=========================================="
echo "🚀 Starting Frontend Server (Vite)..."
echo "=========================================="

cd frontend || { echo "❌ 'frontend' directory not found"; exit 1; }

# Install dependencies if node_modules is missing
if [ ! -d "node_modules" ]; then
  echo "📦 Installing dependencies..."
  npm install
fi

# Run Dev Server
npm run dev

