"""
Django Views Fix for OTP Verification Issues
Add this to your Django views.py file
"""

from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth.models import User
import json
import random
import string
from datetime import datetime, timedelta

# In-memory storage for OTP (replace with database in production)
otp_storage = {}

def generate_otp():
    """Generate a 6-digit OTP"""
    return ''.join(random.choices(string.digits, k=6))

def generate_token():
    """Generate a simple token"""
    return ''.join(random.choices(string.ascii_letters + string.digits, k=32))

@api_view(['POST'])
@permission_classes([AllowAny])
def request_otp(request):
    """
    Request OTP endpoint with detailed logging
    """
    try:
        print("=== OTP Request Started ===")
        data = json.loads(request.body)
        email = data.get('email')
        
        print(f"Email received: {email}")
        print(f"Request headers: {dict(request.headers)}")
        
        if not email:
            print("❌ No email provided")
            return Response({'error': 'Email is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Check if user exists (optional - you might want to allow OTP for non-registered emails)
        try:
            user = User.objects.get(email=email)
            print(f"✅ User found: {user.username}")
        except User.DoesNotExist:
            print(f"⚠️  User not found for email: {email}")
            # You can choose to create a user or just proceed
            # For now, we'll proceed without creating a user
        
        # Generate OTP
        otp = generate_otp()
        print(f"Generated OTP: {otp}")
        
        # Store OTP with timestamp
        otp_storage[email] = {
            'otp': otp,
            'timestamp': datetime.now(),
            'attempts': 0
        }
        print(f"Stored OTP for {email}: {otp}")
        
        # In production, send email here
        # For development, we'll return the OTP in response
        print("✅ OTP request successful")
        
        return Response({
            'message': 'OTP sent successfully',
            'otp': otp  # Remove this in production
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        print("❌ Invalid JSON in request body")
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"❌ OTP request error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([AllowAny])
def verify_otp(request):
    """
    Verify OTP endpoint with detailed logging and error handling
    """
    try:
        print("=== OTP Verification Started ===")
        data = json.loads(request.body)
        email = data.get('email')
        otp = data.get('otp')
        
        print(f"Email: {email}")
        print(f"OTP received: {otp}")
        print(f"OTP type: {type(otp)}")
        print(f"OTP length: {len(str(otp)) if otp else 0}")
        print(f"Request headers: {dict(request.headers)}")
        
        # Validate input
        if not email:
            print("❌ No email provided")
            return Response({'error': 'Email is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        if not otp:
            print("❌ No OTP provided")
            return Response({'error': 'OTP is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Convert OTP to string for comparison
        otp = str(otp).strip()
        print(f"Normalized OTP: '{otp}'")
        
        # Check if OTP exists for this email
        if email not in otp_storage:
            print(f"❌ No OTP found for email: {email}")
            return Response({'error': 'No OTP found for this email. Please request a new OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        
        stored_data = otp_storage[email]
        stored_otp = stored_data['otp']
        timestamp = stored_data['timestamp']
        attempts = stored_data['attempts']
        
        print(f"Stored OTP: '{stored_otp}'")
        print(f"Stored timestamp: {timestamp}")
        print(f"Previous attempts: {attempts}")
        
        # Check OTP expiration (5 minutes)
        expiration_time = timestamp + timedelta(minutes=5)
        current_time = datetime.now()
        
        print(f"Current time: {current_time}")
        print(f"Expiration time: {expiration_time}")
        print(f"OTP expired: {current_time > expiration_time}")
        
        if current_time > expiration_time:
            print("❌ OTP has expired")
            # Remove expired OTP
            del otp_storage[email]
            return Response({'error': 'OTP has expired. Please request a new OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Check attempt limit (3 attempts)
        if attempts >= 3:
            print("❌ Too many verification attempts")
            # Remove OTP after too many attempts
            del otp_storage[email]
            return Response({'error': 'Too many verification attempts. Please request a new OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Increment attempts
        otp_storage[email]['attempts'] += 1
        
        # Compare OTP
        print(f"Comparing OTPs: '{otp}' == '{stored_otp}'")
        if otp == stored_otp:
            print("✅ OTP verification successful")
            
            # Generate token
            token = generate_token()
            print(f"Generated token: {token}")
            
            # Remove OTP from storage after successful verification
            del otp_storage[email]
            
            return Response({
                'message': 'OTP verified successfully',
                'token': token
            }, status=status.HTTP_200_OK)
        else:
            print("❌ OTP verification failed")
            remaining_attempts = 3 - otp_storage[email]['attempts']
            return Response({
                'error': f'Invalid OTP. {remaining_attempts} attempts remaining.'
            }, status=status.HTTP_400_BAD_REQUEST)
        
    except json.JSONDecodeError:
        print("❌ Invalid JSON in request body")
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"❌ OTP verification error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([AllowAny])
def reset_password(request):
    """
    Reset password endpoint with token validation
    """
    try:
        print("=== Password Reset Started ===")
        
        # Get Authorization header
        auth_header = request.headers.get('Authorization', '')
        print(f"Auth header: {auth_header}")
        
        if not auth_header.startswith('Bearer '):
            print("❌ Missing or invalid Authorization header")
            return Response({
                'error': 'Missing or invalid Authorization header. Expected: Bearer <token>'
            }, status=status.HTTP_401_UNAUTHORIZED)
        
        # Extract token
        token = auth_header.split(' ')[1]
        print(f"Extracted token: {token}")
        
        # Parse request body
        data = json.loads(request.body)
        email = data.get('email')
        password = data.get('password')
        confirm_password = data.get('confirm_password')
        
        print(f"Email: {email}")
        print(f"Password length: {len(password) if password else 0}")
        print(f"Confirm password length: {len(confirm_password) if confirm_password else 0}")
        
        # Validate input
        if not email:
            return Response({'error': 'Email is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        if not password:
            return Response({'error': 'Password is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        if not confirm_password:
            return Response({'error': 'Confirm password is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        if password != confirm_password:
            return Response({'error': 'Passwords do not match'}, status=status.HTTP_400_BAD_REQUEST)
        
        if len(password) < 8:
            return Response({'error': 'Password must be at least 8 characters'}, status=status.HTTP_400_BAD_REQUEST)
        
        # For now, accept any non-empty token (in production, validate against stored tokens)
        if not token or token.strip() == '':
            print("❌ Empty token")
            return Response({'error': 'Invalid or missing authentication token'}, status=status.HTTP_401_UNAUTHORIZED)
        
        # Check if user exists
        try:
            user = User.objects.get(email=email)
            print(f"✅ User found: {user.username}")
        except User.DoesNotExist:
            print(f"❌ User not found: {email}")
            return Response({'error': 'User with this email does not exist'}, status=status.HTTP_404_NOT_FOUND)
        
        # Update password
        from django.contrib.auth.hashers import make_password
        user.password = make_password(password)
        user.save()
        
        print(f"✅ Password reset successful for user: {email}")
        
        return Response({
            'message': 'Password reset successful',
            'success': True
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        print("❌ Invalid JSON in request body")
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"❌ Password reset error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

"""
URL Configuration (add to urls.py):
"""
# from django.urls import path
# from . import views
# 
# urlpatterns = [
#     path('api/request-otp/', views.request_otp, name='request_otp'),
#     path('api/verify-otp/', views.verify_otp, name='verify_otp'),
#     path('api/reset-password/', views.reset_password, name='reset_password'),
# ]
