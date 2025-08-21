#!/usr/bin/env python3
"""
Test script to identify OTP verification and token issues
"""

import requests
import json

# Configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms"
EMAIL = "test@example.com"  # Replace with your test email

def test_otp_flow():
    """Test the complete OTP flow and identify token issues"""
    print("🔍 Testing OTP Flow for Token Issues")
    print(f"Email: {EMAIL}")
    print(f"Base URL: {BASE_URL}")
    print("=" * 60)
    
    # Step 1: Request OTP
    print("\n=== Step 1: Request OTP ===")
    url = f"{BASE_URL}/api/request-otp/"
    data = {"email": EMAIL}
    headers = {"Content-Type": "application/json"}
    
    try:
        response = requests.post(url, json=data, headers=headers, timeout=30)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code != 200:
            print("❌ OTP request failed")
            return None
            
        response_data = response.json()
        otp = response_data.get('otp')
        print(f"✅ OTP received: {otp}")
        
    except Exception as e:
        print(f"❌ OTP request error: {e}")
        return None
    
    # Step 2: Verify OTP
    print(f"\n=== Step 2: Verify OTP ===")
    url = f"{BASE_URL}/api/verify-otp/"
    data = {"email": EMAIL, "otp": otp}
    
    try:
        response = requests.post(url, json=data, headers=headers, timeout=30)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code != 200:
            print("❌ OTP verification failed")
            return None
            
        response_data = response.json()
        token = response_data.get('token')
        message = response_data.get('message')
        
        print(f"✅ OTP verification successful")
        print(f"Message: {message}")
        print(f"Token: {token}")
        print(f"Token type: {type(token)}")
        print(f"Token length: {len(str(token)) if token else 0}")
        
        if not token:
            print("⚠️  WARNING: No token in response!")
            print("This is likely the cause of the 'missing authentication token' error")
            return "NO_TOKEN"
        
        return token
        
    except Exception as e:
        print(f"❌ OTP verification error: {e}")
        return None

def test_reset_password_with_token(token):
    """Test password reset with token"""
    print(f"\n=== Step 3: Test Password Reset ===")
    url = f"{BASE_URL}/api/reset-password/"
    data = {
        "email": EMAIL,
        "password": "newpassword123",
        "confirm_password": "newpassword123"
    }
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {token}"
    }
    
    print(f"Using token: {token}")
    print(f"Authorization header: Bearer {token}")
    
    try:
        response = requests.post(url, json=data, headers=headers, timeout=30)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 200:
            print("✅ Password reset successful!")
            return True
        else:
            print("❌ Password reset failed")
            return False
            
    except Exception as e:
        print(f"❌ Password reset error: {e}")
        return False

def test_reset_password_without_token():
    """Test password reset without token (to see if backend accepts it)"""
    print(f"\n=== Step 3: Test Password Reset WITHOUT Token ===")
    url = f"{BASE_URL}/api/reset-password/"
    data = {
        "email": EMAIL,
        "password": "newpassword123",
        "confirm_password": "newpassword123"
    }
    
    headers = {"Content-Type": "application/json"}
    print("No Authorization header")
    
    try:
        response = requests.post(url, json=data, headers=headers, timeout=30)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 200:
            print("✅ Password reset successful WITHOUT token!")
            return True
        else:
            print("❌ Password reset failed without token")
            return False
            
    except Exception as e:
        print(f"❌ Password reset error: {e}")
        return False

def main():
    """Main test flow"""
    # Test OTP flow
    token = test_otp_flow()
    
    if token == "NO_TOKEN":
        print("\n" + "=" * 60)
        print("🔧 ISSUE IDENTIFIED: No token in OTP verification response")
        print("This means your Django backend is not generating/returning a token")
        print("\nPossible solutions:")
        print("1. Update your Django backend to return a token in OTP verification")
        print("2. Use the django_otp_verification_fix.py I provided earlier")
        print("3. Temporarily disable token validation in reset password")
        
        # Test if backend accepts reset without token
        print("\nTesting if backend accepts reset without token...")
        test_reset_password_without_token()
        
    elif token:
        print("\n" + "=" * 60)
        print("✅ Token received successfully")
        test_reset_password_with_token(token)
        
    else:
        print("\n" + "=" * 60)
        print("❌ Could not get token - OTP flow failed")
    
    print("\n" + "=" * 60)
    print("📋 Summary:")
    print("If you see 'No token in response', your Django backend needs to be updated")
    print("Use the django_otp_verification_fix.py file I provided earlier")

if __name__ == "__main__":
    main()
