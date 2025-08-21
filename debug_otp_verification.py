#!/usr/bin/env python3
"""
Debug OTP Verification Issues
"""

import requests
import json

# ⚠️ CHANGE THIS TO YOUR ACTUAL DJANGO SERVER URL
BASE_URL = "http://127.0.0.1:8000"

def debug_request_otp(email):
    """Debug the request OTP endpoint"""
    print(f"🔍 Step 1: Requesting OTP for {email}")
    print("-" * 40)
    
    url = f"{BASE_URL}/api/request-otp/"
    data = {"email": email}
    
    try:
        response = requests.post(url, json=data)
        print(f"📡 Request URL: {url}")
        print(f"📤 Request Data: {data}")
        print(f"📥 Response Status: {response.status_code}")
        print(f"📥 Response Headers: {dict(response.headers)}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"📥 Response Body: {json.dumps(response_data, indent=2)}")
            
            # Check if OTP is in response
            if "otp" in response_data:
                print("✅ SUCCESS: OTP received in response")
                return response_data["otp"]
            else:
                print("❌ FAILED: No OTP in response")
                print("🔍 Available fields:", list(response_data.keys()))
                return None
        else:
            print(f"❌ FAILED: HTTP {response.status_code}")
            try:
                error_data = response.json()
                print(f"📥 Error Response: {json.dumps(error_data, indent=2)}")
            except:
                print(f"📥 Error Response: {response.text}")
            return None
            
    except Exception as e:
        print(f"❌ Network Error: {e}")
        return None

def debug_verify_otp(email, otp):
    """Debug the verify OTP endpoint"""
    print(f"\n🔍 Step 2: Verifying OTP for {email}")
    print("-" * 40)
    
    url = f"{BASE_URL}/api/verify-otp/"
    data = {
        "email": email,
        "otp": otp
    }
    
    print(f"📡 Request URL: {url}")
    print(f"📤 Request Data: {data}")
    
    try:
        response = requests.post(url, json=data)
        print(f"📥 Response Status: {response.status_code}")
        print(f"📥 Response Headers: {dict(response.headers)}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"📥 Response Body: {json.dumps(response_data, indent=2)}")
            
            # Check if token is in response
            if "token" in response_data:
                print("✅ SUCCESS: Token received!")
                return response_data["token"]
            else:
                print("❌ FAILED: No token in response")
                print("🔍 Available fields:", list(response_data.keys()))
                return None
        else:
            print(f"❌ FAILED: HTTP {response.status_code}")
            try:
                error_data = response.json()
                print(f"📥 Error Response: {json.dumps(error_data, indent=2)}")
            except:
                print(f"📥 Error Response: {response.text}")
            return None
            
    except Exception as e:
        print(f"❌ Network Error: {e}")
        return None

def check_django_console():
    """Instructions for checking Django console"""
    print("\n🔍 Step 3: Check Django Console")
    print("-" * 40)
    print("1. Look at your Django server console")
    print("2. You should see these debug messages:")
    print("   ✅ OTP verified for [email]")
    print("   🔑 Generated reset token: [token]...")
    print("   🎯 Returning response with token for [email]")
    print("3. If you don't see these, there's an error in your code")

def main():
    print("🐛 DEBUGGING OTP VERIFICATION")
    print("=" * 60)
    
    # Use a real email from your database
    email = input("Enter a real email from your database: ").strip()
    
    if not email:
        print("❌ Please provide a valid email")
        return
    
    print(f"\n🧪 Testing with email: {email}")
    
    # Step 1: Request OTP
    otp = debug_request_otp(email)
    if not otp:
        print("\n❌ Cannot proceed - OTP request failed")
        check_django_console()
        return
    
    print(f"\n📱 OTP received: {otp}")
    
    # Step 2: Verify OTP
    token = debug_verify_otp(email, otp)
    if not token:
        print("\n❌ Cannot proceed - OTP verification failed")
        check_django_console()
        return
    
    print(f"\n🔑 Token received: {token[:20]}...")
    print("\n✅ SUCCESS: OTP verification working!")
    
    print("\n" + "=" * 60)
    print("🎯 DEBUG COMPLETE!")

if __name__ == "__main__":
    main()
