#!/usr/bin/env python3
"""
Test script to debug token flow in password reset process
"""

import requests
import json

# Configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms"
EMAIL = "test@example.com"  # Replace with your test email

def test_otp_request():
    """Test OTP request"""
    print("=== Testing OTP Request ===")
    url = f"{BASE_URL}/api/request-otp/"
    data = {"email": EMAIL}
    
    try:
        response = requests.post(url, json=data, headers={"Content-Type": "application/json"})
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"✅ OTP Request Success: {response_data}")
            return response_data.get('otp')
        else:
            print(f"❌ OTP Request Failed: {response.text}")
            return None
    except Exception as e:
        print(f"❌ OTP Request Error: {e}")
        return None

def test_otp_verification(otp):
    """Test OTP verification"""
    print("\n=== Testing OTP Verification ===")
    url = f"{BASE_URL}/api/verify-otp/"
    data = {"email": EMAIL, "otp": otp}
    
    try:
        response = requests.post(url, json=data, headers={"Content-Type": "application/json"})
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"✅ OTP Verification Success: {response_data}")
            return response_data.get('token')
        else:
            print(f"❌ OTP Verification Failed: {response.text}")
            return None
    except Exception as e:
        print(f"❌ OTP Verification Error: {e}")
        return None

def test_reset_password(token, new_password="newpassword123"):
    """Test password reset"""
    print("\n=== Testing Password Reset ===")
    url = f"{BASE_URL}/api/reset-password/"
    data = {
        "email": EMAIL,
        "password": new_password,
        "confirm_password": new_password
    }
    
    headers = {
        "Content-Type": "application/json"
    }
    
    # Add Authorization header if token exists
    if token:
        headers["Authorization"] = f"Bearer {token}"
        print(f"Using Authorization header: Bearer {token}")
    else:
        print("⚠️  No token provided - testing without Authorization header")
    
    try:
        response = requests.post(url, json=data, headers=headers)
        print(f"Status Code: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"✅ Password Reset Success: {response_data}")
            return True
        else:
            print(f"❌ Password Reset Failed: {response.text}")
            return False
    except Exception as e:
        print(f"❌ Password Reset Error: {e}")
        return False

def main():
    """Main test flow"""
    print("🔍 Testing Complete Password Reset Flow")
    print(f"Email: {EMAIL}")
    print(f"Base URL: {BASE_URL}")
    print("=" * 50)
    
    # Step 1: Request OTP
    otp = test_otp_request()
    if not otp:
        print("❌ Cannot proceed without OTP")
        return
    
    # Step 2: Verify OTP
    token = test_otp_verification(otp)
    if not token:
        print("❌ Cannot proceed without token")
        return
    
    # Step 3: Reset Password
    success = test_reset_password(token)
    
    print("\n" + "=" * 50)
    if success:
        print("🎉 Complete flow test PASSED")
    else:
        print("💥 Complete flow test FAILED")
    
    print("\n📋 Summary:")
    print(f"OTP: {otp}")
    print(f"Token: {token}")
    print(f"Token Length: {len(token) if token else 0}")

if __name__ == "__main__":
    main()
