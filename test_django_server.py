#!/usr/bin/env python3
"""
Test script to verify Django server is working
Run this after applying the fixes to your Django backend
"""

import requests
import json

# Server configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms/"

def test_otp_request():
    """Test the OTP request endpoint"""
    url = BASE_URL + "api/request-otp/"
    
    # Test data
    test_data = {
        "email": "test@example.com"
    }
    
    print(f"Testing OTP request to: {url}")
    print(f"Test data: {test_data}")
    
    try:
        response = requests.post(url, json=test_data, timeout=30)
        
        print(f"Status Code: {response.status_code}")
        print(f"Response Headers: {dict(response.headers)}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"✅ Success! Response: {response_data}")
            
            if 'otp' in response_data:
                print(f"🎉 OTP received: {response_data['otp']}")
                return True
            else:
                print("⚠️  No OTP in response")
                return False
        else:
            print(f"❌ Error: {response.status_code}")
            print(f"Response: {response.text}")
            return False
            
    except requests.exceptions.Timeout:
        print("❌ Timeout error")
        return False
    except requests.exceptions.ConnectionError:
        print("❌ Connection error - Server might be down")
        return False
    except Exception as e:
        print(f"❌ Error: {e}")
        return False

def test_server_status():
    """Test if server is reachable"""
    try:
        response = requests.get(BASE_URL, timeout=10)
        print(f"Server status: {response.status_code}")
        return True
    except Exception as e:
        print(f"Server not reachable: {e}")
        return False

def main():
    print("Django Server Test")
    print("=" * 50)
    
    # Test server reachability
    if not test_server_status():
        print("❌ Server is not reachable")
        print("Make sure your Django server is running")
        return
    
    print()
    
    # Test OTP request
    if test_otp_request():
        print("\n✅ OTP request is working!")
        print("Your Django backend is properly configured.")
    else:
        print("\n❌ OTP request failed")
        print("Check your Django server logs for errors")

if __name__ == "__main__":
    main()
