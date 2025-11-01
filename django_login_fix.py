"""
Django Login View Fix
This fixes the login endpoint to properly authenticate users with email and password.
The issue was likely that:
1. No proper login view existed
2. Password authentication wasn't using Django's check_password
3. Error messages were not helpful
"""

from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth import authenticate
from django.contrib.auth.models import User
from django.utils import timezone
import json
import secrets
import string

# Store tokens temporarily (in production, use a proper Token model)
token_storage = {}

def generate_token():
    """Generate a secure random token"""
    return ''.join(secrets.choice(string.ascii_letters + string.digits) for _ in range(32))

@api_view(['POST'])
@permission_classes([AllowAny])
def login(request):
    """
    User login endpoint that properly authenticates email and password
    Returns authentication token on success
    """
    try:
        print("=== Login Request Started ===")
        data = json.loads(request.body)
        email = data.get('email', '').strip().lower()
        password = data.get('password', '')
        
        print(f"📧 Email received: {email}")
        print(f"🔐 Password length: {len(password)}")
        
        # Validate input
        if not email:
            print("❌ No email provided")
            return Response(
                {'error': 'Email is required'}, 
                status=status.HTTP_400_BAD_REQUEST
            )
        
        if not password:
            print("❌ No password provided")
            return Response(
                {'error': 'Password is required'}, 
                status=status.HTTP_400_BAD_REQUEST
            )
        
        # Check if user exists by email
        try:
            user = User.objects.get(email=email)
            print(f"✅ User found: {user.username} (ID: {user.id})")
            print(f"📅 User date joined: {user.date_joined}")
            print(f"📅 User last login: {user.last_login}")
            print(f"✅ User is_active: {user.is_active}")
        except User.DoesNotExist:
            print(f"❌ User not found with email: {email}")
            # Don't reveal if email exists or not (security best practice)
            return Response(
                {'error': 'Invalid email or password'}, 
                status=status.HTTP_401_UNAUTHORIZED
            )
        except User.MultipleObjectsReturned:
            print(f"⚠️ Multiple users found with email: {email}")
            # Get the first active user
            user = User.objects.filter(email=email, is_active=True).first()
            if not user:
                return Response(
                    {'error': 'Invalid email or password'}, 
                    status=status.HTTP_401_UNAUTHORIZED
                )
        
        # Check if user is active
        if not user.is_active:
            print(f"❌ User is inactive: {email}")
            return Response(
                {'error': 'Account is inactive. Please contact support.'}, 
                status=status.HTTP_403_FORBIDDEN
            )
        
        # Authenticate user with username and password
        # Django's authenticate() uses username, not email
        # So we need to authenticate with the username
        authenticated_user = authenticate(
            username=user.username, 
            password=password
        )
        
        if authenticated_user is None:
            print(f"❌ Password authentication failed for: {email}")
            # Verify the password manually in case of edge cases
            if user.check_password(password):
                print(f"✅ Password check passed manually for: {email}")
                authenticated_user = user
            else:
                print(f"❌ Password check failed for: {email}")
                return Response(
                    {'error': 'Invalid email or password'}, 
                    status=status.HTTP_401_UNAUTHORIZED
                )
        
        # Update last login timestamp
        user.last_login = timezone.now()
        user.save(update_fields=['last_login'])
        
        # Generate authentication token
        token = generate_token()
        token_storage[email] = {
            'token': token,
            'user_id': user.id,
            'timestamp': timezone.now()
        }
        
        print(f"✅ Login successful for: {email}")
        print(f"🔑 Generated token: {token}")
        
        return Response({
            'message': 'Login successful',
            'token': token,
            'user': {
                'id': user.id,
                'email': user.email,
                'username': user.username
            }
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        print("❌ Invalid JSON in request body")
        return Response(
            {'error': 'Invalid JSON in request body'}, 
            status=status.HTTP_400_BAD_REQUEST
        )
    except Exception as e:
        print(f"❌ Login error: {e}")
        import traceback
        traceback.print_exc()
        return Response(
            {'error': f'Internal server error: {str(e)}'}, 
            status=status.HTTP_500_INTERNAL_SERVER_ERROR
        )


"""
URL Configuration (add to your urls.py):
==========================
from django.urls import path
from .views import login  # or wherever you put this function

urlpatterns = [
    # ... other patterns
    path('api/login/', login, name='login'),
]
"""

"""
Usage in your views.py or a separate auth_views.py:
==========================
1. Import this function or copy the code to your views.py
2. Make sure User model has email field (Django's default User has it)
3. Ensure passwords are stored using Django's password hashing

Testing:
==========================
curl -X POST http://localhost:8000/api/login/ \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

Expected Response:
{
    "message": "Login successful",
    "token": "abc123...",
    "user": {
        "id": 1,
        "email": "user@example.com",
        "username": "username"
    }
}
"""

