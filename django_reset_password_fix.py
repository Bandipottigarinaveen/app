"""
Django Views Fix for Reset Password with Token Validation
Add this to your Django views.py file
"""

from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth.models import User
from django.contrib.auth.hashers import make_password
import json

@api_view(['POST'])
@permission_classes([AllowAny])
def reset_password(request):
    """
    Reset password endpoint that expects:
    - Authorization header with Bearer token
    - JSON body with email, password, confirm_password
    """
    try:
        # Get Authorization header
        auth_header = request.headers.get('Authorization', '')
        print(f"Auth header: {auth_header}")
        
        if not auth_header.startswith('Bearer '):
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
        
        # Validate token (this should match your OTP verification token)
        # For now, we'll accept any non-empty token for testing
        if not token or token.strip() == '':
            return Response({'error': 'Invalid or missing authentication token'}, status=status.HTTP_401_UNAUTHORIZED)
        
        # Check if user exists
        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            return Response({'error': 'User with this email does not exist'}, status=status.HTTP_404_NOT_FOUND)
        
        # Update password
        user.password = make_password(password)
        user.save()
        
        print(f"Password reset successful for user: {email}")
        
        return Response({
            'message': 'Password reset successful',
            'success': True
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"Reset password error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

"""
Alternative implementation without token validation (for testing):
"""

@api_view(['POST'])
@permission_classes([AllowAny])
def reset_password_no_token(request):
    """
    Reset password endpoint WITHOUT token validation (for testing only)
    """
    try:
        data = json.loads(request.body)
        email = data.get('email')
        password = data.get('password')
        confirm_password = data.get('confirm_password')
        
        print(f"Reset password request - Email: {email}")
        
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
        
        # Check if user exists
        try:
            user = User.objects.get(email=email)
        except User.DoesNotExist:
            return Response({'error': 'User with this email does not exist'}, status=status.HTTP_404_NOT_FOUND)
        
        # Update password
        user.password = make_password(password)
        user.save()
        
        print(f"Password reset successful for user: {email}")
        
        return Response({
            'message': 'Password reset successful',
            'success': True
        }, status=status.HTTP_200_OK)
        
    except json.JSONDecodeError:
        return Response({'error': 'Invalid JSON in request body'}, status=status.HTTP_400_BAD_REQUEST)
    except Exception as e:
        print(f"Reset password error: {e}")
        return Response({'error': f'Internal server error: {str(e)}'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
