# Echo Health - Database Relationships

## 🗄️ Entity Relationship Diagram

```mermaid
erDiagram
    USERS {
        int id PK
        string email UK
        string password_hash
        string first_name
        string last_name
        date date_of_birth
        string gender
        string phone_number
        text profile_image_url
        boolean is_verified
        boolean is_active
        timestamp created_at
        timestamp updated_at
    }
    
    OTP_VERIFICATIONS {
        int id PK
        string email
        string otp_code
        string purpose
        timestamp expires_at
        boolean is_used
        int attempts
        timestamp created_at
    }
    
    PASSWORD_RESET_TOKENS {
        int id PK
        int user_id FK
        string token UK
        timestamp expires_at
        boolean is_used
        timestamp created_at
    }
    
    HEALTH_ASSESSMENTS {
        int id PK
        int user_id FK
        string assessment_type
        int risk_score
        string risk_level
        decimal probability
        jsonb assessment_data
        text_array recommendations
        text_array warning_signs
        text_array next_steps
        boolean is_api_result
        timestamp created_at
    }
    
    MEDICAL_REPORTS {
        int id PK
        int user_id FK
        string report_type
        string file_name
        text file_path
        int file_size
        string mime_type
        timestamp upload_date
        string analysis_status
        jsonb analysis_result
        text ai_insights
        text doctor_notes
        boolean is_archived
    }
    
    CHAT_SESSIONS {
        int id PK
        int user_id FK
        string session_type
        boolean is_active
        timestamp started_at
        timestamp ended_at
        int total_messages
    }
    
    CHAT_MESSAGES {
        int id PK
        int session_id FK
        string sender_type
        text message_content
        string message_type
        jsonb metadata
        timestamp created_at
    }
    
    USER_SESSIONS {
        int id PK
        int user_id FK
        string session_token UK
        jsonb device_info
        inet ip_address
        timestamp expires_at
        boolean is_active
        timestamp created_at
        timestamp last_activity
    }
    
    APP_SETTINGS {
        int id PK
        int user_id FK
        string setting_key
        text setting_value
        string setting_type
        timestamp created_at
        timestamp updated_at
    }
    
    %% Relationships
    USERS ||--o{ PASSWORD_RESET_TOKENS : "has"
    USERS ||--o{ HEALTH_ASSESSMENTS : "creates"
    USERS ||--o{ MEDICAL_REPORTS : "uploads"
    USERS ||--o{ CHAT_SESSIONS : "participates"
    USERS ||--o{ USER_SESSIONS : "maintains"
    USERS ||--o{ APP_SETTINGS : "configures"
    
    CHAT_SESSIONS ||--o{ CHAT_MESSAGES : "contains"
```

## 🔗 Key Relationships

### 1. User-Centric Design
- **Users** table is the central entity
- All user-related data references the users table
- Cascade delete ensures data consistency

### 2. Authentication Flow
- **OTP_VERIFICATIONS** - Temporary, no foreign key to users (for unregistered users)
- **PASSWORD_RESET_TOKENS** - Links to users table
- **USER_SESSIONS** - Active user sessions

### 3. Health Data
- **HEALTH_ASSESSMENTS** - Stores all assessment results
- **MEDICAL_REPORTS** - File uploads and analysis results
- Both link to users for personalization

### 4. Communication
- **CHAT_SESSIONS** - Groups related messages
- **CHAT_MESSAGES** - Individual messages within sessions
- Hierarchical relationship for better organization

## 📊 Data Flow Patterns

### 1. User Registration Flow
```
OTP_VERIFICATIONS → USERS → USER_SESSIONS
```

### 2. Health Assessment Flow
```
USERS → HEALTH_ASSESSMENTS → (AI Analysis) → Updated Assessment
```

### 3. Report Upload Flow
```
USERS → MEDICAL_REPORTS → (AI Processing) → Analysis Results
```

### 4. Chat Flow
```
USERS → CHAT_SESSIONS → CHAT_MESSAGES
```

## 🎯 Database Design Principles

### 1. Normalization
- 3NF compliance for data integrity
- Minimal redundancy
- Clear separation of concerns

### 2. Scalability
- Indexed foreign keys
- Partitioning strategy for large tables
- JSONB for flexible data storage

### 3. Security
- No sensitive data in logs
- Encrypted password storage
- Token-based authentication

### 4. Performance
- Strategic indexing
- Query optimization
- Connection pooling
