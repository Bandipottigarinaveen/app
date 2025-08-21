#!/usr/bin/env python3
"""
Test script for token-based password reset flow
"""

import requests
import json

# Configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms"
EMAIL = "test@example.com"  # Replace with your test email

def test_token_based_reset():
    """Test the complete token-based password reset flow"""
    print("🔐 Testing Token-Based Password Reset Flow")
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
            return False
            
        response_data = response.json()
        otp = response_data.get('otp')
        print(f"✅ OTP received: {otp}")
        
    except Exception as e:
        print(f"❌ OTP request error: {e}")
        return False
    
    # Step 2: Verify OTP and get token
    print(f"\n=== Step 2: Verify OTP and Get Token ===")
    url = f"{BASE_URL}/api/verify-otp/"
    data = {"email": EMAIL, "otp": otp}
    
    try:
        response = requests.post(url, json=data, headers=headers, timeout=30)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code != 200:
            print("❌ OTP verification failed")
            return False
            
        response_data = response.json()
        token = response_data.get('token')
        message = response_data.get('message')
        
        print(f"✅ OTP verification successful")
        print(f"Message: {message}")
        print(f"Token: {token}")
        print(f"Token type: {type(token)}")
        print(f"Token length: {len(str(token)) if token else 0}")
        
        if not token:
            print("❌ ERROR: No token in response!")
            print("This means the Django backend is not generating tokens properly")
            return False
        
        return token
        
    except Exception as e:
        print(f"❌ OTP verification error: {e}")
        return False

def test_reset_with_token(token):
    """Test password reset with valid token"""
    print(f"\n=== Step 3: Reset Password WITH Token ===")
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
            print("✅ Password reset successful WITH token!")
            return True
        else:
            print("❌ Password reset failed with token")
            return False
            
    except Exception as e:
        print(f"❌ Password reset error: {e}")
        return False

def test_reset_without_token():
    """Test password reset without token (should fail)"""
    print(f"\n=== Step 4: Test Reset WITHOUT Token (Should Fail) ===")
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
        
        if response.status_code == 401:
            print("✅ Correctly rejected without token!")
            return True
        else:
            print("❌ Should have been rejected without token")
            return False
            
    except Exception as e:
        print(f"❌ Test error: {e}")
        return False

def test_reset_with_invalid_token():
    """Test password reset with invalid token (should fail)"""
    print(f"\n=== Step 5: Test Reset WITH Invalid Token (Should Fail) ===")
    url = f"{BASE_URL}/api/reset-password/"
    data = {
        "email": EMAIL,
        "password": "newpassword123",
        "confirm_password": "newpassword123"
    }
    
    headers = {
        "Content-Type": "application/json",
        "Authorization": "Bearer invalid_token_123"
    }
    
    print("Using invalid token")
    
    try:
        response = requests.post(url, json=data, headers=headers, timeout=30)
        print(f"Status: {response.status_code}")
        print(f"Response: {response.text}")
        
        if response.status_code == 401:
            print("✅ Correctly rejected with invalid token!")
            return True
        else:
            print("❌ Should have been rejected with invalid token")
            return False
            
    except Exception as e:
        print(f"❌ Test error: {e}")
        return False

def main():
    """Main test flow"""
    print("🔐 TOKEN-BASED PASSWORD RESET TEST")
    print("=" * 60)
    
    # Test complete flow
    token = test_token_based_reset()
    
    if token:
        print("\n" + "=" * 60)
        print("✅ Token received successfully")
        
        # Test reset with valid token
        success = test_reset_with_token(token)
        
        if success:
            print("\n" + "=" * 60)
            print("✅ VALID TOKEN FLOW: SUCCESS")
            
            # Test security - should fail without token
            test_reset_without_token()
            
            # Test security - should fail with invalid token
            test_reset_with_invalid_token()
            
        else:
            print("\n" + "=" * 60)
            print("❌ VALID TOKEN FLOW: FAILED")
            
    else:
        print("\n" + "=" * 60)
        print("❌ Could not get token - OTP flow failed")
        print("Make sure your Django backend is updated with the token-based fix")
    
    print("\n" + "=" * 60)
    print("📋 SUMMARY:")
    print("✅ Token-based password reset should work securely")
    print("✅ Reset without token should be rejected")
    print("✅ Reset with invalid token should be rejected")
    print("✅ Use django_reset_password_with_token_fix.py for backend")

if __name__ == "__main__":
    main()
