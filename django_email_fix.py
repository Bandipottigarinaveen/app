# Django Email Configuration Fix
# Add this to your Django settings.py file

# Option 1: Console Backend (for development - emails will be printed to console)
EMAIL_BACKEND = 'django.core.mail.backends.console.EmailBackend'

# Option 2: Gmail SMTP (if you want real emails)
# EMAIL_BACKEND = 'django.core.mail.backends.smtp.EmailBackend'
# EMAIL_HOST = 'smtp.gmail.com'
# EMAIL_PORT = 587
# EMAIL_USE_TLS = True
# EMAIL_HOST_USER = 'your-email@gmail.com'
# EMAIL_HOST_PASSWORD = 'your-app-password'  # Use App Password, not regular password

# Option 3: File Backend (emails saved to files)
# EMAIL_BACKEND = 'django.core.mail.backends.filebased.EmailBackend'
# EMAIL_FILE_PATH = '/tmp/django-emails'  # Directory to store emails

# Option 4: Dummy Backend (emails are not sent at all)
# EMAIL_BACKEND = 'django.core.mail.backends.dummy.EmailBackend'

# Common settings for all backends
DEFAULT_FROM_EMAIL = 'noreply@echohealth.com'
EMAIL_SUBJECT_PREFIX = '[EchoHealth] '
