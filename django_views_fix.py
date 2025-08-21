# Temporary fix for Django views.py
# Replace your current request-otp view with this version

from django.http import JsonResponse
from django.views.decorators.csrf import csrf_exempt
from django.views.decorators.http import require_http_methods
import json
import random
import string
import time

@csrf_exempt
@require_http_methods(["POST"])
def request_otp(request):
    try:
        data = json.loads(request.body)
        email = data.get('email')
        
        if not email:
            return JsonResponse({
                'error': 'Email is required'
            }, status=400)
        
        # Generate a simple 6-digit OTP
        otp = ''.join(random.choices(string.digits, k=6))
        
        # Store OTP in session or cache (temporary solution)
        request.session[f'otp_{email}'] = otp
        request.session[f'otp_time_{email}'] = time.time()
        
        # For development, print OTP to console instead of sending email
        print(f"=== OTP for {email}: {otp} ===")
        print(f"Use this OTP to test your app")
        
        # Return success response
        return JsonResponse({
            'message': 'OTP sent successfully',
            'otp': otp  # Remove this in production!
        }, status=200)
        
    except json.JSONDecodeError:
        return JsonResponse({
            'error': 'Invalid JSON'
        }, status=400)
    except Exception as e:
        print(f"Error in request_otp: {e}")
        return JsonResponse({
            'error': 'Internal server error'
        }, status=500)

@csrf_exempt
@require_http_methods(["POST"])
def verify_otp(request):
    try:
        data = json.loads(request.body)
        email = data.get('email')
        otp = data.get('otp')
        
        if not email or not otp:
            return JsonResponse({
                'error': 'Email and OTP are required'
            }, status=400)
        
        # Get stored OTP
        stored_otp = request.session.get(f'otp_{email}')
        stored_time = request.session.get(f'otp_time_{email}')
        
        if not stored_otp:
            return JsonResponse({
                'error': 'OTP not found or expired'
            }, status=400)
        
        # Check if OTP is expired (5 minutes)
        if time.time() - stored_time > 300:
            # Clear expired OTP
            del request.session[f'otp_{email}']
            del request.session[f'otp_time_{email}']
            return JsonResponse({
                'error': 'OTP expired'
            }, status=400)
        
        # Verify OTP
        if otp == stored_otp:
            # Clear OTP after successful verification
            del request.session[f'otp_{email}']
            del request.session[f'otp_time_{email}']
            
            # Generate a simple token
            token = ''.join(random.choices(string.ascii_letters + string.digits, k=32))
            
            return JsonResponse({
                'message': 'OTP verified successfully',
                'token': token
            }, status=200)
        else:
            return JsonResponse({
                'error': 'Invalid OTP'
            }, status=400)
            
    except json.JSONDecodeError:
        return JsonResponse({
            'error': 'Invalid JSON'
        }, status=400)
    except Exception as e:
        print(f"Error in verify_otp: {e}")
        return JsonResponse({
            'error': 'Internal server error'
        }, status=500)
