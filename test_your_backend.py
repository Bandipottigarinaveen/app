#!/usr/bin/env python3
"""
Test Your Django Backend
Replace BASE_URL with your actual Django server URL
"""

import requests
import json

# ⚠️ CHANGE THIS TO YOUR ACTUAL DJANGO SERVER URL
BASE_URL = "http://127.0.0.1:8000"  # or your actual server URL

def test_request_otp():
    """Test the request OTP endpoint"""
    print("🔍 Testing Request OTP...")
    
    url = f"{BASE_URL}/api/request-otp/"
    data = {
        "email": "test@example.com"  # Use a real email from your database
    }
    
    try:
        response = requests.post(url, json=data)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        return response.json()
    except Exception as e:
        print(f"❌ Error: {e}")
        return None

def test_verify_otp(email, otp):
    """Test the verify OTP endpoint"""
    print(f"\n🔍 Testing Verify OTP for {email}...")
    
    url = f"{BASE_URL}/api/verify-otp/"
    data = {
        "email": email,
        "otp": otp
    }
    
    try:
        response = requests.post(url, json=data)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 200:
            response_data = response.json()
            if "token" in response_data:
                print("✅ SUCCESS: Token received!")
                return response_data["token"]
            else:
                print("❌ FAILED: No token in response")
                return None
        else:
            print("❌ FAILED: API call failed")
            return None
            
    except Exception as e:
        print(f"❌ Error: {e}")
        return None

def test_reset_password(email, token, password):
    """Test the reset password endpoint"""
    print(f"\n🔍 Testing Reset Password for {email}...")
    
    url = f"{BASE_URL}/api/reset-password/"
    data = {
        "email": email,
        "password": password,
        "confirm_password": password
    }
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    try:
        response = requests.post(url, json=data, headers=headers)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.json()}")
        
        if response.status_code == 200:
            print("✅ SUCCESS: Password reset successful!")
        else:
            print("❌ FAILED: Password reset failed")
            
    except Exception as e:
        print(f"❌ Error: {e}")

def main():
    print("🧪 TESTING YOUR DJANGO BACKEND")
    print("=" * 50)
    
    # Step 1: Request OTP
    otp_response = test_request_otp()
    if not otp_response:
        print("❌ Cannot proceed without OTP")
        return
    
    # Extract OTP from response (for testing)
    otp = otp_response.get("otp")
    if not otp:
        print("❌ No OTP in response")
        return
    
    email = "test@example.com"  # Use the same email
    
    # Step 2: Verify OTP
    token = test_verify_otp(email, otp)
    if not token:
        print("❌ Cannot proceed without token")
        return
    
    # Step 3: Reset Password
    test_reset_password(email, token, "newpassword123")
    
    print("\n" + "=" * 50)
    print("🎯 TEST COMPLETE!")

if __name__ == "__main__":
    main()
