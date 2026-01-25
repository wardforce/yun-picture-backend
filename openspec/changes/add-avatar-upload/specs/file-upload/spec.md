## ADDED Requirements

### Requirement: Avatar Upload Capability
The file-upload subsystem SHALL support dedicated avatar upload with WebP compression.

#### Scenario: Avatar key generation
- **WHEN** avatar is uploaded for user ID 123
- **THEN** the system generates COS key as `avatar/{userId}_{timestamp}.webp`

#### Scenario: Validation constraints for avatars
- **WHEN** avatar upload is initiated
- **THEN** the system validates: file size ≤ 2MB, format in [jpg, jpeg, png, webp], proper MIME type

#### Scenario: Avatar compression via COS
- **WHEN** avatar is uploaded
- **THEN** COS automatically applies `imageMogr2/format/webp` rule, storing only WebP format without original
