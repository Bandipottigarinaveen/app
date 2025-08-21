#!/usr/bin/env python3
"""
Test Script for Backend Token Fix
Run this to test if your backend is working correctly
"""

import requests
import json
import time

# Configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms"
TEST_EMAIL = "test@example.com"
TEST_PASSWORD = "newpassword123"

def test_backend_token_flow():
    """Test the complete backend token flow"""
    print("🧪 Testing Backend Token Flow")
    print("=" * 50)
    
    # Test 1: Request OTP
    print("\n1️⃣ Testing OTP Request")
    otp_url = f"{BASE_URL}/api/request-otp/"
    otp_data = {"email": TEST_EMAIL}
    
    try:
        otp_response = requests.post(otp_url, json=otp_data, timeout=10)
        print(f"OTP Request Status: {otp_response.status_code}")
        
        if otp_response.status_code == 200:
            otp_body = otp_response.json()
            print(f"✅ OTP Request Success: {otp_body}")
            test_otp = otp_body.get('otp')
            if test_otp:
                print(f"🔐 Received OTP: {test_otp}")
            else:
                print("❌ No OTP in response")
                return False
        else:
            print(f"❌ OTP Request Failed: {otp_response.text}")
            return False
            
    except Exception as e:
        print(f"❌ OTP Request Error: {e}")
        return False
    
    # Wait a moment
    time.sleep(1)
    
    # Test 2: Verify OTP
    print("\n2️⃣ Testing OTP Verification")
    verify_url = f"{BASE_URL}/api/verify-otp/"
    verify_data = {"email": TEST_EMAIL, "otp": test_otp}
    
    try:
        verify_response = requests.post(verify_url, json=verify_data, timeout=10)
        print(f"OTP Verification Status: {verify_response.status_code}")
        
        if verify_response.status_code == 200:
            verify_body = verify_response.json()
            print(f"✅ OTP Verification Success: {verify_body}")
            
            # Check if token is returned
            reset_token = verify_body.get('token')
            if reset_token:
                print(f"🔑 Reset Token Received: {reset_token}")
                print(f"📏 Token Length: {len(reset_token)}")
            else:
                print("❌ No reset token in response!")
                print("This is the main issue - backend not returning token")
                return False
        else:
            print(f"❌ OTP Verification Failed: {verify_response.text}")
            return False
            
    except Exception as e:
        print(f"❌ OTP Verification Error: {e}")
        return False
    
    # Wait a moment
    time.sleep(1)
    
    # Test 3: Reset Password
    print("\n3️⃣ Testing Password Reset")
    reset_url = f"{BASE_URL}/api/reset-password/"
    reset_data = {
        "email": TEST_EMAIL,
        "password": TEST_PASSWORD,
        "confirm_password": TEST_PASSWORD
    }
    reset_headers = {
        "Authorization": f"Bearer {reset_token}",
        "Content-Type": "application/json"
    }
    
    try:
        reset_response = requests.post(reset_url, json=reset_data, headers=reset_headers, timeout=10)
        print(f"Password Reset Status: {reset_response.status_code}")
        
        if reset_response.status_code == 200:
            reset_body = reset_response.json()
            print(f"✅ Password Reset Success: {reset_body}")
        else:
            print(f"❌ Password Reset Failed: {reset_response.text}")
            return False
            
    except Exception as e:
        print(f"❌ Password Reset Error: {e}")
        return False
    
    print("\n✅ All backend tests passed!")
    return True

def test_backend_endpoints():
    """Test if backend endpoints are accessible"""
    print("🔍 Testing Backend Endpoints")
    print("=" * 30)
    
    endpoints = [
        "/",
        "/api/request-otp/",
        "/api/verify-otp/",
        "/api/reset-password/"
    ]
    
    for endpoint in endpoints:
        try:
            url = f"{BASE_URL}{endpoint}"
            response = requests.get(url, timeout=5)
            print(f"✅ {endpoint}: {response.status_code}")
        except Exception as e:
            print(f"❌ {endpoint}: {e}")

if __name__ == "__main__":
    print("🚀 Starting Backend Token Tests")
    print(f"📍 Base URL: {BASE_URL}")
    print(f"📧 Test Email: {TEST_EMAIL}")
    
    # Test endpoint accessibility
    test_backend_endpoints()
    
    print("\n" + "="*50)
    
    # Test complete flow
    success = test_backend_token_flow()
    
    if success:
        print("\n🎉 Backend is working correctly!")
        print("✅ Token generation: Working")
        print("✅ Token storage: Working") 
        print("✅ Token validation: Working")
    else:
        print("\n❌ Backend has issues!")
        print("🔧 Check the error messages above")
        print("🔧 Make sure your Django server is running")
        print("🔧 Verify the views are properly implemented")
