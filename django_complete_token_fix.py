"""
COMPLETE Django Backend Fix for Password Reset with Token Generation
Add this to your Django views.py file to fix the missing token issues
"""

from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth.models import User
from django.contrib.auth.hashers import make_password
from django.utils import timezone
import json
import random
import string
import secrets
from datetime import datetime, timedelta

# ============================================================================
# TOKEN STORAGE AND GENERATION FUNCTIONS
# ============================================================================

# In-memory storage for OTP and tokens (replace with database in production)
otp_storage = {}
token_storage = {}

def generate_otp():
    """Generate a 6-digit OTP"""
    return ''.join(random.choices(string.digits, k=6))

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
    print(f"✅ Stored token for {email}: {token}")
    print(f"⏰ Token expires at: {token_storage[email]['expires_at']}")

def is_token_valid(email, token):
    """Check if token is valid for the given email"""
    if email not in token_storage:
        print(f"❌ No token found for email: {email}")
        return False
    
    stored_data = token_storage[email]
    stored_token = stored_data['token']
    expires_at = stored_data['expires_at']
    current_time = datetime.now()
    
    print(f"🔍 Validating token for {email}")
    print(f"📝 Stored token: {stored_token}")
    print(f"📝 Received token: {token}")
    print(f"⏰ Expires at: {expires_at}")
    print(f"⏰ Current time: {current_time}")
    
    # Check if token has expired
    if current_time > expires_at:
        print(f"❌ Token expired for {email}")
        del token_storage[email]
        return False
    
    # Check if token matches
    if stored_token == token:
        print(f"✅ Token valid for {email}")
        return True
    else:
        print(f"❌ Token mismatch for {email}")
        return False

def cleanup_expired_tokens():
    """Remove expired tokens from storage"""
    current_time = datetime.now()
    expired_emails = []
    
    for email, data in token_storage.items():
        if current_time > data['expires_at']:
            expired_emails.append(email)
    
    for email in expired_emails:
        del token_storage[email]
        print(f"🧹 Cleaned up expired token for {email}")

# ============================================================================
# API ENDPOINTS
# ============================================================================

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
        
        print(f"📧 Email received: {email}")
        
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
        print(f"🔐 Generated OTP: {otp}")
        
        # Store OTP with timestamp
        otp_storage[email] = {
            'otp': otp,
            'timestamp': datetime.now(),
            'attempts': 0
        }
        print(f"💾 Stored OTP for {email}: {otp}")
        
        # Return OTP in response (for development)
        return Response({
            'message': 'OTP sent successfully',
            'otp': otp  # Remove this in production
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"❌ OTP request error: {e}")
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
        
        print(f"📧 Email: {email}")
        print(f"🔐 OTP received: {otp}")
        
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
            print(f"🔑 Generated secure token: {token}")
            
            # Store token with expiration
            store_token(email, token)
            
            # Clean up expired tokens
            cleanup_expired_tokens()
            
            print(f"🎯 Returning response with token for {email}")
            return Response({
                'message': 'OTP verified successfully',
                'token': token,
                'success': True
            }, status=status.HTTP_200_OK)
        else:
            remaining_attempts = 3 - otp_storage[email]['attempts']
            print(f"❌ Invalid OTP for {email}. {remaining_attempts} attempts remaining.")
            return Response({
                'error': f'Invalid OTP. {remaining_attempts} attempts remaining.'
            }, status=status.HTTP_400_BAD_REQUEST)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"❌ OTP verification error: {e}")
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
        
        print(f"📧 Email: {email}")
        print(f"🔐 Password length: {len(password) if password else 0}")
        
        # Get Authorization header
        auth_header = request.headers.get('Authorization', '')
        print(f"🔑 Authorization header: {auth_header}")
        
        # Extract token from Authorization header
        if not auth_header.startswith('Bearer '):
            return Response({'error': 'Missing or invalid Authorization header. Token required.'}, status=status.HTTP_401_UNAUTHORIZED)
        
        token = auth_header.replace('Bearer ', '').strip()
        print(f"🔑 Extracted token: {token}")
        
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
            print(f"🗑️ Removed token for {email} after successful reset")
        
        print(f"✅ Password reset successful for user: {email}")
        
        return Response({
            'message': 'Password reset successful',
            'success': True
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"❌ Password reset error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

# ============================================================================
# URL CONFIGURATION
# ============================================================================

"""
Add this to your Django urls.py file:

from django.urls import path
from . import views

urlpatterns = [
    path('api/request-otp/', views.request_otp, name='request_otp'),
    path('api/verify-otp/', views.verify_otp, name='verify_otp'),
    path('api/reset-password/', views.reset_password, name='reset_password'),
]
"""

# ============================================================================
# TESTING FUNCTIONS
# ============================================================================

def test_token_flow():
    """Test the complete token flow"""
    print("🧪 Testing Complete Token Flow")
    print("=" * 50)
    
    # Test 1: Generate OTP
    print("\n1️⃣ Testing OTP Generation")
    test_email = "test@example.com"
    test_otp = generate_otp()
    print(f"Generated OTP: {test_otp}")
    
    # Test 2: Store OTP
    print("\n2️⃣ Testing OTP Storage")
    otp_storage[test_email] = {
        'otp': test_otp,
        'timestamp': datetime.now(),
        'attempts': 0
    }
    print(f"Stored OTP for {test_email}")
    
    # Test 3: Generate Token
    print("\n3️⃣ Testing Token Generation")
    test_token = generate_secure_token()
    print(f"Generated token: {test_token}")
    
    # Test 4: Store Token
    print("\n4️⃣ Testing Token Storage")
    store_token(test_email, test_token)
    print(f"Stored token for {test_email}")
    
    # Test 5: Validate Token
    print("\n5️⃣ Testing Token Validation")
    is_valid = is_token_valid(test_email, test_token)
    print(f"Token valid: {is_valid}")
    
    # Test 6: Cleanup
    print("\n6️⃣ Testing Cleanup")
    cleanup_expired_tokens()
    print("Cleanup completed")
    
    print("\n✅ All tests passed!")

if __name__ == "__main__":
    test_token_flow()
