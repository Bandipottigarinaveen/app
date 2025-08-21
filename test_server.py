#!/usr/bin/env python3
"""
Simple script to test server connectivity
Run this from your development machine to check if the server is reachable
"""

import requests
import sys
from urllib.parse import urljoin

# Server configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms/"
ENDPOINTS = [
    "api/request-otp/",
    "api/login/",
    "api/register/"
]

def test_endpoint(url, endpoint):
    """Test a specific endpoint"""
    full_url = urljoin(url, endpoint)
    print(f"Testing: {full_url}")
    
    try:
        # Test with a simple POST request
        test_data = {"email": "test@test.com"}
        response = requests.post(full_url, json=test_data, timeout=30)
        
        print(f"  Status Code: {response.status_code}")
        print(f"  Response: {response.text[:200]}...")
        
        if response.status_code in [200, 400, 401, 422]:
            print(f"  ✅ Endpoint is reachable")
            return True
        else:
            print(f"  ❌ Unexpected status code")
            return False
            
    except requests.exceptions.Timeout:
        print(f"  ❌ Timeout error")
        return False
    except requests.exceptions.ConnectionError:
        print(f"  ❌ Connection error")
        return False
    except Exception as e:
        print(f"  ❌ Error: {e}")
        return False

def main():
    print("Server Connectivity Test")
    print("=" * 50)
    print(f"Base URL: {BASE_URL}")
    print()
    
    # Test base URL first
    try:
        response = requests.get(BASE_URL, timeout=10)
        print(f"Base URL test: {response.status_code}")
    except Exception as e:
        print(f"Base URL test failed: {e}")
    
    print()
    
    # Test each endpoint
    success_count = 0
    for endpoint in ENDPOINTS:
        if test_endpoint(BASE_URL, endpoint):
            success_count += 1
        print()
    
    print("=" * 50)
    print(f"Results: {success_count}/{len(ENDPOINTS)} endpoints reachable")
    
    if success_count == 0:
        print("❌ Server appears to be unreachable")
        print("Possible issues:")
        print("1. Dev tunnel is down or expired")
        print("2. Server is not running")
        print("3. Network connectivity issues")
        print("4. Firewall blocking the connection")
    elif success_count < len(ENDPOINTS):
        print("⚠️  Some endpoints are reachable, others are not")
    else:
        print("✅ All endpoints are reachable")

if __name__ == "__main__":
    main()
