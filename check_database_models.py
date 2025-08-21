#!/usr/bin/env python3
"""
Check Database Models and Setup
"""

import os
import sys
import django

# Add your Django project path here
DJANGO_PROJECT_PATH = "C:/path/to/your/django/project"  # CHANGE THIS

def setup_django():
    """Setup Django environment"""
    try:
        os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'your_project.settings')  # CHANGE THIS
        sys.path.append(DJANGO_PROJECT_PATH)
        django.setup()
        print("✅ Django setup successful")
        return True
    except Exception as e:
        print(f"❌ Django setup failed: {e}")
        return False

def check_models():
    """Check if models exist and are properly configured"""
    try:
        from django.apps import apps
        from django.db import connection
        
        print("\n🔍 CHECKING MODELS:")
        print("-" * 40)
        
        # Check if PasswordResetOTP model exists
        try:
            from your_app.models import PasswordResetOTP  # CHANGE THIS
            print("✅ PasswordResetOTP model found")
            
            # Check model fields
            fields = PasswordResetOTP._meta.fields
            print(f"📋 Model fields: {[field.name for field in fields]}")
            
            # Check if is_expired method exists
            if hasattr(PasswordResetOTP, 'is_expired'):
                print("✅ is_expired method exists")
            else:
                print("❌ is_expired method missing")
                
        except ImportError as e:
            print(f"❌ PasswordResetOTP model not found: {e}")
            return False
        
        # Check database connection
        try:
            with connection.cursor() as cursor:
                cursor.execute("SELECT 1")
                print("✅ Database connection working")
        except Exception as e:
            print(f"❌ Database connection failed: {e}")
            return False
        
        # Check if table exists
        try:
            with connection.cursor() as cursor:
                cursor.execute("""
                    SELECT name FROM sqlite_master 
                    WHERE type='table' AND name='your_app_passwordresetotp'
                """)
                tables = cursor.fetchall()
                if tables:
                    print("✅ PasswordResetOTP table exists")
                else:
                    print("❌ PasswordResetOTP table missing")
                    
                # Check table structure
                cursor.execute("PRAGMA table_info(your_app_passwordresetotp)")
                columns = cursor.fetchall()
                print(f"📋 Table columns: {[col[1] for col in columns]}")
                
        except Exception as e:
            print(f"❌ Table check failed: {e}")
            
        return True
        
    except Exception as e:
        print(f"❌ Model check failed: {e}")
        return False

def check_migrations():
    """Check migration status"""
    try:
        print("\n🔍 CHECKING MIGRATIONS:")
        print("-" * 40)
        
        from django.core.management import execute_from_command_line
        
        # Check migration status
        execute_from_command_line(['manage.py', 'showmigrations'])
        
        return True
        
    except Exception as e:
        print(f"❌ Migration check failed: {e}")
        return False

def create_test_otp():
    """Try to create a test OTP entry"""
    try:
        print("\n🔍 TESTING OTP CREATION:")
        print("-" * 40)
        
        from your_app.models import PasswordResetOTP  # CHANGE THIS
        
        # Try to create a test OTP
        test_otp = PasswordResetOTP.objects.create(
            email="test@example.com",
            otp="123456"
        )
        print("✅ Test OTP created successfully")
        
        # Check if it was saved
        saved_otp = PasswordResetOTP.objects.get(email="test@example.com")
        print(f"📋 Saved OTP: {saved_otp.otp}")
        
        # Test is_expired method
        if hasattr(saved_otp, 'is_expired'):
            expired = saved_otp.is_expired()
            print(f"⏰ OTP expired: {expired}")
        
        # Clean up
        test_otp.delete()
        print("🧹 Test OTP cleaned up")
        
        return True
        
    except Exception as e:
        print(f"❌ OTP creation test failed: {e}")
        return False

def main():
    print("🔍 DATABASE DIAGNOSTICS")
    print("=" * 50)
    
    print("⚠️  BEFORE RUNNING THIS SCRIPT:")
    print("1. Change DJANGO_PROJECT_PATH to your actual Django project path")
    print("2. Change 'your_project' to your actual project name")
    print("3. Change 'your_app' to your actual app name")
    print("4. Make sure your Django server is not running")
    
    # Check if paths are configured
    if "C:/path/to/your/django/project" in DJANGO_PROJECT_PATH:
        print("\n❌ Please configure the correct paths first!")
        return
    
    if not setup_django():
        return
    
    if not check_models():
        return
    
    if not check_migrations():
        return
    
    if not create_test_otp():
        return
    
    print("\n" + "=" * 50)
    print("🎯 DATABASE CHECK COMPLETE!")

if __name__ == "__main__":
    main()
