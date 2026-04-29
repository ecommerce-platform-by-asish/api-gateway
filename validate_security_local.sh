#!/bin/bash

# Security E2E Validation Script
# This script validates Rate Limiting and Token Blacklisting against a running environment.

GATEWAY_URL=${1:-"http://localhost:8080"}
AUTH_SERVICE_URL=${2:-"http://localhost:8081"}

echo "Using Gateway URL: $GATEWAY_URL"
echo "Using Auth Service URL: $AUTH_SERVICE_URL"

# Helper for login
login_user() {
    echo "Logging in to get JWT..."
    RESPONSE=$(curl -s -X POST "$GATEWAY_URL/api/auth/login" \
        -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"password"}')
    TOKEN=$(echo $RESPONSE | grep -oE '"accessToken":"[^"]+"' | cut -d'"' -f4)
    if [ -z "$TOKEN" ]; then
        echo "Failed to login!"
        exit 1
    fi
    echo "Login successful."
}

# 1. Validate Rate Limiting
validate_rate_limit() {
    echo "--------------------------------------------------"
    echo "Validating Rate Limiting..."
    echo "Sending 15 rapid requests to /api/auth/test..."
    
    SUCCESS_COUNT=0
    LIMIT_COUNT=0
    
    for i in {1..15}; do
        STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/api/auth/test")
        if [ "$STATUS" == "429" ]; then
            ((LIMIT_COUNT++))
        else
            ((SUCCESS_COUNT++))
        fi
    done
    
    echo "Results: Success=$SUCCESS_COUNT, Rate Limited (429)=$LIMIT_COUNT"
    if [ $LIMIT_COUNT -gt 0 ]; then
        echo "✅ Rate Limiting is working!"
    else
        echo "❌ Rate Limiting FAILED (no 429 received)."
    fi
}

# 2. Validate Token Blacklisting (via Logout)
validate_blacklist() {
    echo "--------------------------------------------------"
    echo "Validating Token Blacklisting..."
    login_user
    
    echo "Verifying token works before logout..."
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/api/products" \
        -H "Authorization: Bearer $TOKEN")
    
    if [ "$STATUS" == "401" ] || [ "$STATUS" == "403" ]; then
         echo "Initial request failed with status $STATUS. Check if services are running."
         return
    fi
    echo "Token verified (Status $STATUS)."

    echo "Logging out to blacklist token..."
    curl -s -X POST "$GATEWAY_URL/api/auth/logout" \
        -H "Authorization: Bearer $TOKEN"
    
    echo "Verifying token is rejected after logout..."
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/api/products" \
        -H "Authorization: Bearer $TOKEN")
    
    if [ "$STATUS" == "401" ]; then
        echo "✅ Token Blacklisting is working! (Received 401)"
    else
        echo "❌ Token Blacklisting FAILED (Status $STATUS instead of 401)."
    fi
}

# Run validations
validate_rate_limit
validate_blacklist
