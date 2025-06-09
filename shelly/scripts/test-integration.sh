#!/bin/bash

# RGBW Control App - Integration Test Script
# Tests the complete workflow from API to Shelly bulb

echo "🧪 Testing RGBW Control App Integration..."
echo "========================================="

# Test 1: Health Check
echo "1. Testing health endpoint..."
HEALTH_RESPONSE=$(curl -s http://localhost:8080/health)
if [[ $HEALTH_RESPONSE == *"RGBW Control Service is running"* ]]; then
    echo "✅ Health check passed"
else
    echo "❌ Health check failed: $HEALTH_RESPONSE"
    exit 1
fi

# Test 2: Color API with various colors
echo "2. Testing color API with different colors..."

# Test Red
echo "   Testing Red (255,0,0)..."
RED_RESPONSE=$(curl -s -X POST http://localhost:8080/color -H "Content-Type: application/json" -d '{"red":255,"green":0,"blue":0}')
if [[ $RED_RESPONSE == *"success\":true"* ]]; then
    echo "   ✅ Red color test passed"
else
    echo "   ❌ Red color test failed: $RED_RESPONSE"
fi

sleep 2

# Test Green
echo "   Testing Green (0,255,0)..."
GREEN_RESPONSE=$(curl -s -X POST http://localhost:8080/color -H "Content-Type: application/json" -d '{"red":0,"green":255,"blue":0}')
if [[ $GREEN_RESPONSE == *"success\":true"* ]]; then
    echo "   ✅ Green color test passed"
else
    echo "   ❌ Green color test failed: $GREEN_RESPONSE"
fi

sleep 2

# Test Blue
echo "   Testing Blue (0,0,255)..."
BLUE_RESPONSE=$(curl -s -X POST http://localhost:8080/color -H "Content-Type: application/json" -d '{"red":0,"green":0,"blue":255}')
if [[ $BLUE_RESPONSE == *"success\":true"* ]]; then
    echo "   ✅ Blue color test passed"
else
    echo "   ❌ Blue color test failed: $BLUE_RESPONSE"
fi

sleep 2

# Test White
echo "   Testing White (255,255,255)..."
WHITE_RESPONSE=$(curl -s -X POST http://localhost:8080/color -H "Content-Type: application/json" -d '{"red":255,"green":255,"blue":255}')
if [[ $WHITE_RESPONSE == *"success\":true"* ]]; then
    echo "   ✅ White color test passed"
else
    echo "   ❌ White color test failed: $WHITE_RESPONSE"
fi

sleep 2

# Test 3: Error handling
echo "3. Testing error handling..."
ERROR_RESPONSE=$(curl -s -X POST http://localhost:8080/color -H "Content-Type: application/json" -d '{"red":300,"green":0,"blue":0}')
if [[ $ERROR_RESPONSE == *"success\":false"* ]]; then
    echo "   ✅ Error handling test passed"
else
    echo "   ❌ Error handling test failed: $ERROR_RESPONSE"
fi

# Test 4: Frontend accessibility
echo "4. Testing frontend accessibility..."
FRONTEND_RESPONSE=$(curl -s -I http://localhost:8080/)
if [[ $FRONTEND_RESPONSE == *"302"* ]]; then
    echo "   ✅ Frontend redirect working"
else
    echo "   ❌ Frontend redirect failed"
fi

HTML_RESPONSE=$(curl -s -I http://localhost:8080/index.html)
if [[ $HTML_RESPONSE == *"200"* ]]; then
    echo "   ✅ Frontend HTML accessible"
else
    echo "   ❌ Frontend HTML not accessible"
fi

JS_RESPONSE=$(curl -s -I http://localhost:8080/app.js)
if [[ $JS_RESPONSE == *"200"* ]]; then
    echo "   ✅ Frontend JavaScript accessible"
else
    echo "   ❌ Frontend JavaScript not accessible"
fi

echo ""
echo "🎉 Integration testing complete!"
echo "📱 Open http://localhost:8080 in your browser to test the full application"
echo "🎥 Grant camera permissions to test color detection"
echo "💡 Ensure Shelly bulb is connected to see color changes"
