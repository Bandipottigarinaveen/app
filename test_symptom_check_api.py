#!/usr/bin/env python3
"""
Test script for Symptom Check API integration
Tests the /api/predict/ endpoint
"""

import requests
import json

# Configuration
BASE_URL = "https://rr0cpmfz-8000.inc1.devtunnels.ms"

def test_symptom_check_api():
    """Test the symptom check API endpoint"""
    print("🧪 Testing Symptom Check API")
    print("=" * 50)
    
    # Test data - high risk case
    test_data = {
        "mode": "symptoms",
        "features": {
            "Age": 45,
            "Gender": "Male",
            "Tobacco Use": "Yes",
            "Alcohol Consumption": "Yes",
            "HPV Infection": "No",
            "Betel Quid Use": "Yes",
            "Poor Oral Hygiene": "Yes",
            "Oral Lesions": "Yes",
            "Unexplained Bleeding": "Yes",
            "Difficulty Swallowing": "No",
            "White or Red Patches in Mouth": "Yes",
            "Oral Cancer (Diagnosis)": "No"
        }
    }
    
    url = f"{BASE_URL}/api/predict/"
    headers = {"Content-Type": "application/json"}
    
    print(f"📡 Testing endpoint: {url}")
    print(f"📊 Test data: {json.dumps(test_data, indent=2)}")
    
    try:
        response = requests.post(url, json=test_data, headers=headers, timeout=30)
        print(f"\n📈 Response Status: {response.status_code}")
        print(f"📋 Response Headers: {dict(response.headers)}")
        
        if response.status_code == 200:
            response_data = response.json()
            print(f"✅ API Response:")
            print(f"   Risk Score: {response_data.get('risk_score', 'N/A')}")
            print(f"   Risk Level: {response_data.get('risk_level', 'N/A')}")
            print(f"   Probability: {response_data.get('probability', 'N/A')}")
            print(f"   Message: {response_data.get('message', 'N/A')}")
            
            if 'recommendations' in response_data:
                print(f"   Recommendations: {len(response_data['recommendations'])} items")
                for i, rec in enumerate(response_data['recommendations'], 1):
                    print(f"     {i}. {rec}")
            
            if 'warning_signs' in response_data:
                print(f"   Warning Signs: {len(response_data['warning_signs'])} items")
                for i, sign in enumerate(response_data['warning_signs'], 1):
                    print(f"     {i}. {sign}")
            
            if 'next_steps' in response_data:
                print(f"   Next Steps: {len(response_data['next_steps'])} items")
                for i, step in enumerate(response_data['next_steps'], 1):
                    print(f"     {i}. {step}")
            
            print("\n🎉 Symptom Check API is working correctly!")
            return True
            
        else:
            print(f"❌ API Error: {response.status_code}")
            print(f"Error Response: {response.text}")
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

def test_multiple_scenarios():
    """Test multiple risk scenarios"""
    print("\n" + "=" * 50)
    print("🔄 Testing Multiple Risk Scenarios")
    print("=" * 50)
    
    scenarios = [
        {
            "name": "Low Risk",
            "data": {
                "mode": "symptoms",
                "features": {
                    "Age": 25,
                    "Gender": "Female",
                    "Tobacco Use": "No",
                    "Alcohol Consumption": "No",
                    "HPV Infection": "No",
                    "Betel Quid Use": "No",
                    "Poor Oral Hygiene": "No",
                    "Oral Lesions": "No",
                    "Unexplained Bleeding": "No",
                    "Difficulty Swallowing": "No",
                    "White or Red Patches in Mouth": "No",
                    "Oral Cancer (Diagnosis)": "No"
                }
            }
        },
        {
            "name": "Moderate Risk",
            "data": {
                "mode": "symptoms",
                "features": {
                    "Age": 35,
                    "Gender": "Male",
                    "Tobacco Use": "Yes",
                    "Alcohol Consumption": "No",
                    "HPV Infection": "No",
                    "Betel Quid Use": "No",
                    "Poor Oral Hygiene": "Yes",
                    "Oral Lesions": "No",
                    "Unexplained Bleeding": "No",
                    "Difficulty Swallowing": "No",
                    "White or Red Patches in Mouth": "No",
                    "Oral Cancer (Diagnosis)": "No"
                }
            }
        },
        {
            "name": "High Risk",
            "data": {
                "mode": "symptoms",
                "features": {
                    "Age": 55,
                    "Gender": "Male",
                    "Tobacco Use": "Yes",
                    "Alcohol Consumption": "Yes",
                    "HPV Infection": "Yes",
                    "Betel Quid Use": "Yes",
                    "Poor Oral Hygiene": "Yes",
                    "Oral Lesions": "Yes",
                    "Unexplained Bleeding": "Yes",
                    "Difficulty Swallowing": "Yes",
                    "White or Red Patches in Mouth": "Yes",
                    "Oral Cancer (Diagnosis)": "No"
                }
            }
        }
    ]
    
    url = f"{BASE_URL}/api/predict/"
    headers = {"Content-Type": "application/json"}
    
    for scenario in scenarios:
        print(f"\n📊 Testing {scenario['name']} Scenario:")
        try:
            response = requests.post(url, json=scenario['data'], headers=headers, timeout=15)
            if response.status_code == 200:
                data = response.json()
                print(f"   ✅ Risk Score: {data.get('risk_score', 'N/A')}")
                print(f"   ✅ Risk Level: {data.get('risk_level', 'N/A')}")
                print(f"   ✅ Probability: {data.get('probability', 'N/A')}")
            else:
                print(f"   ❌ Error: {response.status_code} - {response.text}")
        except Exception as e:
            print(f"   ❌ Error: {e}")

if __name__ == "__main__":
    print("🚀 Starting Symptom Check API Tests")
    print(f"📍 Base URL: {BASE_URL}")
    
    # Test basic functionality
    success = test_symptom_check_api()
    
    if success:
        # Test multiple scenarios
        test_multiple_scenarios()
        
        print("\n" + "=" * 50)
        print("✅ All tests completed!")
        print("📱 The Android app should now work with the API")
        print("🔧 If tests fail, check your backend implementation")
    else:
        print("\n" + "=" * 50)
        print("❌ Basic API test failed!")
        print("🔧 Check your backend implementation")
        print("🔧 Make sure the /api/predict/ endpoint is working")
