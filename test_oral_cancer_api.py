#!/usr/bin/env python3
"""
Test script for Oral Cancer Detection API
Tests the /api/oral-cancer-detect/ endpoint
"""

import requests
import json
import base64
import io

# Try to import PIL, fallback to basic image creation
try:
    from PIL import Image
    PIL_AVAILABLE = True
except ImportError:
    PIL_AVAILABLE = False
    print("⚠️ PIL not available, using basic image creation")

# Configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms"

def create_test_image_base64():
    """Create a small test image and convert to base64"""
    if PIL_AVAILABLE:
        # Create a simple test image using PIL
        img = Image.new('RGB', (100, 100), color='red')
        buffer = io.BytesIO()
        img.save(buffer, format='JPEG', quality=80)
        img_bytes = buffer.getvalue()
    else:
        # Create a minimal JPEG header for testing
        # This is a very basic 1x1 red pixel JPEG
        jpeg_header = b'\xff\xd8\xff\xe0\x00\x10JFIF\x00\x01\x01\x01\x00H\x00H\x00\x00\xff\xdb\x00C\x00\x08\x06\x06\x07\x06\x05\x08\x07\x07\x07\t\t\x08\n\x0c\x14\r\x0c\x0b\x0b\x0c\x19\x12\x13\x0f\x14\x1d\x1a\x1f\x1e\x1d\x1a\x1c\x1c $.\' ",#\x1c\x1c(7),01444\x1f\'9=82<.342\xff\xc0\x00\x11\x08\x00\x01\x00\x01\x01\x01\x11\x00\x02\x11\x01\x03\x11\x01\xff\xc4\x00\x14\x00\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x08\xff\xc4\x00\x14\x10\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xff\xda\x00\x0c\x03\x01\x00\x02\x11\x03\x11\x00\x3f\x00\xaa\xff\xd9'
        img_bytes = jpeg_header
    
    # Convert to base64
    base64_string = base64.b64encode(img_bytes).decode('utf-8')
    return base64_string

def test_oral_cancer_detection_api():
    """Test the oral cancer detection API endpoint"""
    print("🧪 Testing Oral Cancer Detection API")
    print("=" * 50)
    
    # Create test image
    print("📸 Creating test image...")
    base64_image = create_test_image_base64()
    print(f"✅ Test image created (Base64 length: {len(base64_image)})")
    
    # Test data
    test_data = {
        "image": base64_image
    }
    
    url = f"{BASE_URL}/api/oral-cancer-detect/"
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    
    print(f"📡 Testing endpoint: {url}")
    print(f"📊 Request headers: {headers}")
    print(f"📊 Request body size: {len(json.dumps(test_data))} bytes")
    
    try:
        response = requests.post(url, json=test_data, headers=headers, timeout=30)
        print(f"\n📈 Response Status: {response.status_code}")
        print(f"📋 Response Headers: {dict(response.headers)}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"✅ API Response:")
            print(f"   Success: {response_data.get('success', 'N/A')}")
            print(f"   Message: {response_data.get('message', 'N/A')}")
            print(f"   Prediction: {response_data.get('prediction', 'N/A')}")
            print(f"   Confidence: {response_data.get('confidence', 'N/A')}")
            print(f"   Risk Level: {response_data.get('risk_level', 'N/A')}")
            
            if 'recommendations' in response_data and response_data['recommendations']:
                print(f"   Recommendations: {len(response_data['recommendations'])} items")
                for i, rec in enumerate(response_data['recommendations'], 1):
                    print(f"     {i}. {rec}")
            
            print("\n🎉 Oral Cancer Detection API is working correctly!")
            return True
            
        else:
            print(f"❌ API Error: {response.status_code}")
            print(f"Error Response: {response.text}")
            
            # Try to parse error response
            try:
                error_data = response.json()
                print(f"Error Details: {json.dumps(error_data, indent=2)}")
            except:
                print("Could not parse error response as JSON")
            
            return False
            
    except requests.exceptions.Timeout:
        print("❌ Request timed out")
        return False
    except requests.exceptions.ConnectionError:
        print("❌ Connection error - check if server is running")
        return False
    except Exception as e:
        print(f"❌ Unexpected error: {e}")
        return False

def test_with_different_formats():
    """Test with different base64 formats"""
    print("\n" + "=" * 50)
    print("🔄 Testing Different Base64 Formats")
    print("=" * 50)
    
    base64_image = create_test_image_base64()
    
    # Test different request formats
    test_cases = [
        {
            "name": "Standard format",
            "data": {"image": base64_image}
        },
        {
            "name": "With data URL prefix",
            "data": {"image": f"data:image/jpeg;base64,{base64_image}"}
        },
        {
            "name": "With additional fields",
            "data": {
                "image": base64_image,
                "format": "jpeg",
                "quality": 80
            }
        }
    ]
    
    url = f"{BASE_URL}/api/oral-cancer-detect/"
    headers = {"Content-Type": "application/json"}
    
    for test_case in test_cases:
        print(f"\n📊 Testing {test_case['name']}:")
        try:
            response = requests.post(url, json=test_case['data'], headers=headers, timeout=15)
            print(f"   Status: {response.status_code}")
            if response.status_code != 200:
                print(f"   Error: {response.text[:200]}...")
        except Exception as e:
            print(f"   Error: {e}")

if __name__ == "__main__":
    try:
        print("🚀 Starting Oral Cancer Detection API Tests")
        print(f"📍 Base URL: {BASE_URL}")
        
        # Test basic functionality
        success = test_oral_cancer_detection_api()
        
        if not success:
            # Test different formats
            test_with_different_formats()
            
            print("\n" + "=" * 50)
            print("❌ API test failed!")
            print("🔧 Check your backend implementation")
            print("🔧 Make sure the /api/oral-cancer-detect/ endpoint is working")
        else:
            print("\n" + "=" * 50)
            print("✅ API test successful!")
            print("📱 The Android app should work with this format")
    except Exception as e:
        print(f"❌ Script error: {e}")
        import traceback
        traceback.print_exc()
