# Echo Health - Detailed App Flow

## 📱 Complete User Journey

```mermaid
flowchart TD
    Start([App Launch]) --> CheckAuth{User Authenticated?}
    
    %% Authentication Flow
    CheckAuth -->|No| AuthChoice{Choose Action}
    AuthChoice -->|Login| LoginPage[Login Page]
    AuthChoice -->|Signup| SignupPage[Signup Page]
    AuthChoice -->|Forgot Password| ForgotPass[Forgot Password]
    
    %% Login Flow
    LoginPage --> LoginSubmit[Submit Credentials]
    LoginSubmit --> LoginCheck{Valid Credentials?}
    LoginCheck -->|Yes| Dashboard[Dashboard]
    LoginCheck -->|No| LoginError[Show Error]
    LoginError --> LoginPage
    
    %% Signup Flow
    SignupPage --> SignupSubmit[Submit Details]
    SignupSubmit --> OTPRequest[Request OTP]
    OTPRequest --> OTPVerify[OTP Verification]
    OTPVerify --> OTPCheck{Valid OTP?}
    OTPCheck -->|Yes| Dashboard
    OTPCheck -->|No| OTPError[OTP Error]
    OTPError --> OTPVerify
    
    %% Forgot Password Flow
    ForgotPass --> ForgotSubmit[Submit Email]
    ForgotSubmit --> ForgotOTP[Request OTP]
    ForgotOTP --> ForgotOTPVerify[OTP Verification]
    ForgotOTPVerify --> ForgotOTPCheck{Valid OTP?}
    ForgotOTPCheck -->|Yes| ResetPass[Reset Password]
    ForgotOTPCheck -->|No| ForgotError[OTP Error]
    ForgotError --> ForgotOTPVerify
    ResetPass --> LoginPage
    
    %% Main App Flow
    CheckAuth -->|Yes| Dashboard
    Dashboard --> MainMenu{Select Feature}
    
    %% Health Assessment Flow
    MainMenu -->|Health Check| SymptomChecker[Symptom Checker]
    SymptomChecker --> FillForm[Fill Assessment Form]
    FillForm --> SubmitForm[Submit Form]
    SubmitForm --> CheckNetwork{Network Available?}
    CheckNetwork -->|Yes| APICall[Call AI API]
    CheckNetwork -->|No| OfflineCalc[Offline Calculation]
    APICall --> APISuccess{API Success?}
    APISuccess -->|Yes| AIResults[AI-Powered Results]
    APISuccess -->|No| OfflineCalc
    OfflineCalc --> BasicResults[Basic Results]
    AIResults --> ResultsPage[Results Display]
    BasicResults --> ResultsPage
    
    %% Report Upload Flow
    MainMenu -->|Upload Reports| ReportUpload[Report Upload]
    ReportUpload --> SelectSource{Select Source}
    SelectSource -->|Camera| TakePhoto[Take Photo]
    SelectSource -->|Gallery| SelectImage[Select Image]
    TakePhoto --> ProcessImage[Process Image]
    SelectImage --> ProcessImage
    ProcessImage --> UploadFile[Upload to Server]
    UploadFile --> AnalysisStatus[Analysis Status]
    AnalysisStatus --> ReportResults[Report Analysis]
    
    %% Chat Support Flow
    MainMenu -->|Chat Support| ChatSupport[Chat Support]
    ChatSupport --> ChatType{Select Chat Type}
    ChatType -->|General| GeneralChat[General Chat]
    ChatType -->|Medical| MedicalChat[Medical Chat]
    ChatType -->|Support| SupportChat[Support Chat]
    GeneralChat --> ChatInterface[Chat Interface]
    MedicalChat --> ChatInterface
    SupportChat --> ChatInterface
    ChatInterface --> SendMessage[Send Message]
    SendMessage --> AIResponse[AI Response]
    AIResponse --> ChatInterface
    
    %% Profile Management Flow
    MainMenu -->|Profile| ProfilePage[Profile Page]
    ProfilePage --> ProfileOptions{Profile Options}
    ProfileOptions -->|Edit Profile| EditProfile[Edit Profile]
    ProfileOptions -->|Change Password| ChangePass[Change Password]
    ProfileOptions -->|Settings| AppSettings[App Settings]
    ProfileOptions -->|Logout| LogoutConfirm[Confirm Logout]
    LogoutConfirm -->|Yes| Logout[Logout User]
    LogoutConfirm -->|No| ProfilePage
    Logout --> CheckAuth
    
    %% Results and Navigation
    ResultsPage --> ResultActions{Result Actions}
    ResultActions -->|Save Report| SaveReport[Save to Profile]
    ResultActions -->|Share| ShareReport[Share Results]
    ResultActions -->|New Assessment| SymptomChecker
    ResultActions -->|Back to Dashboard| Dashboard
    
    SaveReport --> Dashboard
    ShareReport --> Dashboard
    ReportResults --> Dashboard
    ChatInterface --> Dashboard
    EditProfile --> ProfilePage
    ChangePass --> ProfilePage
    AppSettings --> ProfilePage
```

## 🔄 State Management Flow

```mermaid
stateDiagram-v2
    [*] --> Unauthenticated
    
    Unauthenticated --> Login : User Login
    Unauthenticated --> Signup : User Registration
    Unauthenticated --> ForgotPassword : Password Reset
    
    Login --> Authenticated : Login Success
    Login --> Unauthenticated : Login Failed
    
    Signup --> OTPVerification : Registration Success
    OTPVerification --> Authenticated : OTP Valid
    OTPVerification --> Unauthenticated : OTP Invalid
    
    ForgotPassword --> OTPVerification : Email Sent
    OTPVerification --> PasswordReset : OTP Valid
    PasswordReset --> Authenticated : Password Reset Success
    
    Authenticated --> Dashboard : App Ready
    Dashboard --> HealthAssessment : Start Assessment
    Dashboard --> ReportUpload : Upload Reports
    Dashboard --> ChatSupport : Start Chat
    Dashboard --> Profile : Manage Profile
    
    HealthAssessment --> AssessmentComplete : Form Submitted
    AssessmentComplete --> Dashboard : Return to Dashboard
    
    ReportUpload --> UploadComplete : File Processed
    UploadComplete --> Dashboard : Return to Dashboard
    
    ChatSupport --> ChatActive : Chat Started
    ChatActive --> Dashboard : End Chat
    
    Profile --> Dashboard : Profile Updated
    Profile --> Unauthenticated : Logout
    
    Authenticated --> Unauthenticated : Session Expired
```

## 📊 Data Flow Architecture

```mermaid
graph LR
    subgraph "Mobile App"
        UI[User Interface]
        VM[ViewModels]
        Repo[Repositories]
        LocalDB[(Local Database)]
    end
    
    subgraph "Network Layer"
        Retrofit[Retrofit Client]
        Interceptors[Auth Interceptors]
        Cache[Response Cache]
    end
    
    subgraph "Backend API"
        Auth[Authentication]
        Health[Health Assessment]
        Reports[Report Processing]
        Chat[Chat Service]
    end
    
    subgraph "External Services"
        AI[AI/ML Models]
        Storage[File Storage]
        Notifications[Push Notifications]
    end
    
    UI --> VM
    VM --> Repo
    Repo --> LocalDB
    Repo --> Retrofit
    Retrofit --> Interceptors
    Interceptors --> Cache
    Cache --> Auth
    Cache --> Health
    Cache --> Reports
    Cache --> Chat
    
    Health --> AI
    Reports --> AI
    Reports --> Storage
    Chat --> Notifications
```

## 🎯 Key Features Flow

### 1. Health Assessment Flow
```
User Input → Form Validation → API Call → AI Processing → Results Display → Save Assessment
```

### 2. Report Upload Flow
```
File Selection → Image Processing → Upload → AI Analysis → Results → Save Report
```

### 3. Chat Support Flow
```
Chat Initiation → Message Sending → AI Processing → Response → Continue Chat
```

### 4. Authentication Flow
```
Credentials → Validation → Token Generation → Session Management → App Access
```

## 🔐 Security Flow

```mermaid
sequenceDiagram
    participant U as User
    participant A as App
    participant API as Backend API
    participant DB as Database
    
    U->>A: Enter Credentials
    A->>API: Login Request
    API->>DB: Validate Credentials
    DB-->>API: User Data
    API->>API: Generate JWT Token
    API-->>A: Token + User Data
    A->>A: Store Token Securely
    A->>API: Subsequent Requests (with Token)
    API->>API: Validate Token
    API-->>A: Protected Data
```

## 📈 Performance Optimization Flow

### 1. Caching Strategy
```
API Request → Check Cache → Cache Hit? → Return Cached Data
                    ↓
                Cache Miss → API Call → Store in Cache → Return Data
```

### 2. Offline Support
```
User Action → Check Network → Online? → API Call
                    ↓
                Offline → Local Processing → Cache for Sync → Return Local Result
```

### 3. Background Sync
```
App Background → Check Pending Actions → Network Available? → Sync Data
```

This comprehensive flow design ensures a smooth, secure, and efficient user experience while maintaining data integrity and system performance.
