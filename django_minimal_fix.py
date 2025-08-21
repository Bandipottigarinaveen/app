"""
MINIMAL Django Backend Fix - Copy this directly into your views.py
This fixes the missing token generation issue
"""

import secrets
from datetime import datetime, timedelta

# ============================================================================
# ADD THESE FUNCTIONS TO YOUR views.py
# ============================================================================

# Add these storage dictionaries at the top of your views.py file
otp_storage = {}
token_storage = {}

def generate_secure_token():
    """Generate a secure token for password reset"""
    return secrets.token_urlsafe(32)

def store_token(email, token):
    """Store token with expiration (10 minutes)"""
    token_storage[email] = {
        'token': token,
        'timestamp': datetime.now(),
        'expires_at': datetime.now() + timedelta(minutes=10)
    }

def is_token_valid(email, token):
    """Check if token is valid for the given email"""
    if email not in token_storage:
        return False
    
    stored_data = token_storage[email]
    stored_token = stored_data['token']
    expires_at = stored_data['expires_at']
    current_time = datetime.now()
    
    if current_time > expires_at:
        del token_storage[email]
        return False
    
    return stored_token == token

# ============================================================================
# REPLACE YOUR verify_otp VIEW WITH THIS
# ============================================================================

@api_view(['POST'])
def verify_otp(request):
    """
    Verify OTP endpoint - Returns a secure token for password reset
    """
    try:
        data = json.loads(request.body)
        email = data.get('email')
        otp = data.get('otp')
        
        if not email or not otp:
            return Response({'error': 'Email and OTP are required'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Check if OTP exists for this email
        if email not in otp_storage:
            return Response({'error': 'No OTP found for this email. Please request a new OTP.'}, status=status.HTTP_400_BAD_REQUEST)
        
        stored_data = otp_storage[email]
        stored_otp = stored_data['otp']
        timestamp = stored_data['timestamp']
        attempts = stored_data.get('attempts', 0)
        
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
        if 'attempts' not in otp_storage[email]:
            otp_storage[email]['attempts'] = 0
        otp_storage[email]['attempts'] += 1
        
        # Compare OTP
        if str(otp).strip() == str(stored_otp).strip():
            print("✅ OTP verification successful")
            
            # Remove OTP from storage after successful verification
            del otp_storage[email]
            
            # Generate secure token for password reset
            token = generate_secure_token()
            print(f"🔑 Generated secure token: {token}")
            
            # Store token with expiration
            store_token(email, token)
            
            print(f"🎯 Returning response with token for {email}")
            return Response({
                'message': 'OTP verified successfully',
                'token': token,  # ← THIS IS THE KEY FIX
                'success': True
            }, status=status.HTTP_200_OK)
        else:
            remaining_attempts = 3 - otp_storage[email]['attempts']
            return Response({
                'error': f'Invalid OTP. {remaining_attempts} attempts remaining.'
            }, status=status.HTTP_400_BAD_REQUEST)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"❌ OTP verification error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

# ============================================================================
# UPDATE YOUR reset_password VIEW TO INCLUDE THIS
# ============================================================================

@api_view(['POST'])
def reset_password(request):
    """
    Reset password endpoint WITH token validation
    """
    try:
        data = json.loads(request.body)
        email = data.get('email')
        password = data.get('password')
        confirm_password = data.get('confirm_password')
        
        # Get Authorization header
        auth_header = request.headers.get('Authorization', '')
        
        # Extract token from Authorization header
        if not auth_header.startswith('Bearer '):
            return Response({'error': 'Missing or invalid Authorization header. Token required.'}, status=status.HTTP_401_UNAUTHORIZED)
        
        token = auth_header.replace('Bearer ', '').strip()
        
        if not token:
            return Response({'error': 'Authentication token is required'}, status=status.HTTP_401_UNAUTHORIZED)
        
        # Validate input
        if not email or not password or not confirm_password:
            return Response({'error': 'Email, password, and confirm_password are required'}, status=status.HTTP_400_BAD_REQUEST)
        
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
        except User.DoesNotExist:
            return Response({'error': 'User with this email does not exist'}, status=status.HTTP_404_NOT_FOUND)
        
        # Update password
        user.password = make_password(password)
        user.save()
        
        # Remove token after successful password reset
        if email in token_storage:
            del token_storage[email]
        
        return Response({
            'message': 'Password reset successful',
            'success': True
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

# ============================================================================
# ADD THESE IMPORTS TO YOUR views.py
# ============================================================================

"""
Make sure you have these imports at the top of your views.py:

import json
import secrets
from datetime import datetime, timedelta
from django.contrib.auth.hashers import make_password
from django.contrib.auth.models import User
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
"""
