## MODIFIED Requirements

### Requirement: User Profile Display
The system SHALL display user avatar information in profile queries.

#### Scenario: Retrieve user profile with avatar
- **WHEN** a user retrieves their profile via `GET /api/user/get/login`
- **THEN** the response includes the `userAvatar` field (URL or null)

## ADDED Requirements

### Requirement: Avatar Upload from File
The system SHALL allow users to upload avatar images as files.

#### Scenario: Upload avatar from multipart file
- **WHEN** user calls `POST /api/user/avatar/upload` with multipart file (jpg/png/webp, max 2MB)
- **THEN** the system uploads to COS with WebP compression, deletes old avatar, updates user record, and returns the new avatar URL

#### Scenario: Reject oversized avatar file
- **WHEN** user uploads file larger than 2MB
- **THEN** the system returns HTTP 400 with error "头像大小不能超过2MB"

#### Scenario: Reject invalid avatar format
- **WHEN** user uploads file not in jpg/jpeg/png/webp format
- **THEN** the system returns HTTP 400 with error "头像格式仅支持jpg/png/webp"

### Requirement: Avatar Upload from URL
The system SHALL allow users to upload avatar images from external URLs.

#### Scenario: Upload avatar from URL
- **WHEN** user calls `POST /api/user/avatar/upload/url` with valid HTTP(S) image URL
- **THEN** the system downloads the image, uploads to COS with WebP compression, deletes old avatar, updates user record, and returns the new avatar URL

#### Scenario: Reject malformed URL
- **WHEN** user provides invalid URL format
- **THEN** the system returns HTTP 400 with error "文件地址格式不正确"

#### Scenario: Reject non-HTTP URL
- **WHEN** user provides URL with non-HTTP/HTTPS protocol
- **THEN** the system returns HTTP 400 with error "仅支持http或https协议的文件地址"

### Requirement: Avatar Storage and Compression
Avatars SHALL be stored in COS with WebP compression to optimize storage.

#### Scenario: WebP format conversion and storage
- **WHEN** avatar is uploaded (file or URL)
- **THEN** the system automatically converts to WebP format via COS image processing, stores under `avatar/{userId}_{timestamp}.webp`, and reduces storage by 60-70% compared to original format

#### Scenario: Old avatar cleanup
- **WHEN** user updates avatar
- **THEN** the system deletes the old avatar from COS; deletion failures are logged but do not block the update
