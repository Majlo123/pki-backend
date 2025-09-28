# JWT Session Tracking and Revocation System

## Overview

This system provides comprehensive JWT token management with session tracking and revocation capabilities for the PKI backend. It maintains a database of active JWT sessions and allows for controlled revocation of tokens.

## Features

1. **JWT Token Generation**: Secure JWT tokens with configurable expiration
2. **Session Tracking**: Database storage of active sessions with metadata
3. **Token Revocation**: Individual and bulk session revocation
4. **Automatic Cleanup**: Scheduled cleanup of expired sessions
5. **Session Management**: View and manage active sessions per user

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login and receive JWT token
- `POST /api/auth/register` - User registration
- `GET /api/auth/confirm-email` - Email confirmation

### Session Management
- `GET /api/sessions/active` - Get user's active sessions
- `POST /api/sessions/revoke` - Revoke current session
- `POST /api/sessions/revoke-all` - Revoke all user sessions
- `POST /api/sessions/revoke/{sessionId}` - Revoke specific session
- `POST /api/sessions/cleanup` - Cleanup expired sessions (admin)

## Usage Examples

### 1. Login and Get JWT Token

```bash
curl -X POST https://localhost:8443/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "email": "user@example.com",
    "role": "USER",
    "enabled": true
  }
}
```

### 2. Use JWT Token for Authenticated Requests

```bash
curl -X GET https://localhost:8443/api/sessions/active \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. Revoke Current Session

```bash
curl -X POST https://localhost:8443/api/sessions/revoke \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. Revoke All Sessions

```bash
curl -X POST https://localhost:8443/api/sessions/revoke-all \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Configuration

The JWT system can be configured in `application.properties`:

```properties
# JWT Configuration
app.jwt.secret=mySecretKey123456789012345678901234567890123456789012345
app.jwt.expiration=3600000  # 1 hour in milliseconds
```

## Database Schema

The system creates a `jwt_sessions` table with the following structure:

```sql
CREATE TABLE jwt_sessions (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(1000) UNIQUE NOT NULL,
    username VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_accessed_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    user_agent VARCHAR(255),
    ip_address VARCHAR(45)
);
```

## Security Features

1. **Token Blacklisting**: Revoked tokens are stored in database and checked on each request
2. **Session Expiry**: Automatic cleanup of expired sessions
3. **User Control**: Users can view and manage their active sessions
4. **Secure Storage**: Tokens are stored securely with metadata

## Implementation Details

### Key Components

1. **JwtService**: Handles token generation, validation, and revocation
2. **JwtSessionService**: Manages session database operations
3. **JwtAuthenticationFilter**: Intercepts requests to validate tokens
4. **SessionController**: Provides session management endpoints
5. **SessionCleanupService**: Automated cleanup of expired sessions

### Security Flow

1. User logs in → JWT token generated and stored in database
2. Each request → Token validated against database and expiration
3. Token revoked → Marked as inactive in database
4. Scheduled cleanup → Removes old inactive sessions

## Error Handling

The system provides appropriate HTTP status codes:

- `200 OK`: Successful operation
- `401 Unauthorized`: Invalid or revoked token
- `400 Bad Request`: Invalid request format
- `404 Not Found`: Session not found

## Best Practices

1. **Token Expiry**: Set appropriate expiration times based on security needs
2. **Session Limits**: Consider limiting number of active sessions per user
3. **Cleanup Schedule**: Regular cleanup of expired sessions
4. **Secure Headers**: Always use HTTPS for token transmission
5. **Token Storage**: Store tokens securely on client side

## Migration from Basic Auth

If you were previously using Basic Authentication, you can gradually migrate:

1. Keep both authentication methods enabled
2. Update frontend to use JWT tokens
3. Monitor usage and gradually disable Basic Auth
4. Remove Basic Auth once migration is complete
