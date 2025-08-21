"""
Django Views Fix for Password Reset WITH Proper Token Validation
Add this to your Django views.py file for secure token-based password reset
"""

from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth.models import User
from django.contrib.auth.hashers import make_password
import json
import random
import string
import secrets
from datetime import datetime, timedelta

# In-memory storage for OTP and tokens (replace with database in production)
otp_storage = {}
token_storage = {}

def generate_otp():
    """Generate a 6-digit OTP"""
    return ''.join(random.choices(string.digits, k=6))

def generate_secure_token():
    """Generate a secure token for password reset"""
    return secrets.token_urlsafe(32)

def is_token_valid(email, token):
    """Check if token is valid for the given email"""
    if email not in token_storage:
        return False
    
    stored_data = token_storage[email]
    stored_token = stored_data['token']
    timestamp = stored_data['timestamp']
    
    # Token expires after 10 minutes
    expiration_time = timestamp + timedelta(minutes=10)
    current_time = datetime.now()
    
    if current_time > expiration_time:
        # Remove expired token
        del token_storage[email]
        return False
    
    return stored_token == token

@api_view(['POST'])
@permission_classes([AllowAny])
def request_otp(request):
    """
    Request OTP endpoint
    """
    try:
        print("=== OTP Request Started ===")
        data = json.loads(request.body)
        email = data.get('email')
        
        print(f"Email received: {email}")
        
        if not email:
            return Response({'error': 'Email is required'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Check if user exists
        try:
            user = User.objects.get(email=email)
            print(f"✅ User found: {user.username}")
        except User.DoesNotExist:
            print(f"❌ User not found: {email}")
            return Response({'error': 'User with this email does not exist'}, status=status.HTTP_404_NOT_FOUND)
        
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
        
        # Return OTP in response (for development)
        return Response({
            'message': 'OTP sent successfully',
            'otp': otp  # Remove this in production
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"OTP request error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([AllowAny])
def verify_otp(request):
    """
    Verify OTP endpoint - Returns a secure token for password reset
    """
    try:
        print("=== OTP Verification Started ===")
        data = json.loads(request.body)
        email = data.get('email')
        otp = data.get('otp')
        
        print(f"Email: {email}")
        print(f"OTP received: {otp}")
        
        if not email or not otp:
            return Response({'error': 'Email and OTP are required'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Convert OTP to string for comparison
        otp = str(otp).strip()
        
        # Check if OTP exists for this email
        if email not in otp_storage:
            return Response({'error': 'No OTP found for this email. Please request a new OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        
        stored_data = otp_storage[email]
        stored_otp = stored_data['otp']
        timestamp = stored_data['timestamp']
        attempts = stored_data['attempts']
        
        # Check OTP expiration (5 minutes)
        expiration_time = timestamp + timedelta(minutes=5)
        current_time = datetime.now()
        
        if current_time > expiration_time:
            del otp_storage[email]
            return Response({'error': 'OTP has expired. Please request a new OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Check attempt limit (3 attempts)
        if attempts >= 3:
            del otp_storage[email]
            return Response({'error': 'Too many verification attempts. Please request a new OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Increment attempts
        otp_storage[email]['attempts'] += 1
        
        # Compare OTP
        if otp == stored_otp:
            print("✅ OTP verification successful")
            
            # Remove OTP from storage after successful verification
            del otp_storage[email]
            
            # Generate secure token for password reset
            token = generate_secure_token()
            print(f"Generated secure token: {token}")
            
            # Store token with timestamp
            token_storage[email] = {
                'token': token,
                'timestamp': datetime.now()
            }
            print(f"Stored token for {email}: {token}")
            
            return Response({
                'message': 'OTP verified successfully',
                'token': token
            }, status=status.HTTP_200_OK)
        else:
            remaining_attempts = 3 - otp_storage[email]['attempts']
            return Response({
                'error': f'Invalid OTP. {remaining_attempts} attempts remaining.'
            }, status=status.HTTP_400_BAD_REQUEST)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"OTP verification error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([AllowAny])
def reset_password(request):
    """
    Reset password endpoint WITH token validation
    """
    try:
        print("=== Password Reset Started ===")
        
        # Parse request body
        data = json.loads(request.body)
        email = data.get('email')
        password = data.get('password')
        confirm_password = data.get('confirm_password')
        
        print(f"Email: {email}")
        print(f"Password length: {len(password) if password else 0}")
        
        # Get Authorization header
        auth_header = request.headers.get('Authorization', '')
        print(f"Authorization header: {auth_header}")
        
        # Extract token from Authorization header
        if not auth_header.startswith('Bearer '):
            return Response({'error': 'Missing or invalid Authorization header. Token required.'}, status=status.HTTP_401_UNAUTHORIZED)
        
        token = auth_header.replace('Bearer ', '').strip()
        print(f"Extracted token: {token}")
        
        if not token:
            return Response({'error': 'Authentication token is required'}, status=status.HTTP_401_UNAUTHORIZED)
        
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
        
        # Validate token
        if not is_token_valid(email, token):
            return Response({'error': 'Invalid or expired authentication token. Please verify OTP again.'}, status=status.HTTP_401_UNAUTHORIZED)
        
        # Check if user exists
        try:
            user = User.objects.get(email=email)
            print(f"✅ User found: {user.username}")
        except User.DoesNotExist:
            print(f"❌ User not found: {email}")
            return Response({'error': 'User with this email does not exist'}, status=status.HTTP_404_NOT_FOUND)
        
        # Update password
        user.password = make_password(password)
        user.save()
        
        # Remove token after successful password reset
        if email in token_storage:
            del token_storage[email]
        
        print(f"✅ Password reset successful for user: {email}")
        
        return Response({
            'message': 'Password reset successful',
            'success': True
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"Password reset error: {e}")
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
