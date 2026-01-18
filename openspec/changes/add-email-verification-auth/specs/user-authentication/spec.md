# User Authentication - Email Verification Code

这是用户认证功能的邮箱验证码模块规范，定义邮箱验证码登录和密码重置功能的行为要求。

## ADDED Requirements

### Requirement: Email Verification Code Generation

系统 SHALL 生成 6 位纯数字验证码用于邮箱验证。

验证码 SHALL 具备以下特性：
- 格式：6 位纯数字（000000-999999）
- 随机生成，无规律可循
- 每次请求生成新验证码

#### Scenario: Generate 6-digit numeric code

- **WHEN** 系统需要生成验证码
- **THEN** 系统生成 6 位纯数字验证码
- **AND** 验证码范围在 000000 到 999999 之间
- **AND** 验证码具有足够随机性

### Requirement: Email Verification Code Storage

系统 SHALL 将验证码存储在 Redis 中，并设置 5 分钟有效期。

存储规则：
- Key 格式：`yun-picture:email:code:{email}`
- Value：6 位验证码字符串
- TTL：300 秒（5 分钟）
- 同一邮箱的新验证码 SHALL 覆盖旧验证码

#### Scenario: Store code in Redis with 5-minute TTL

- **WHEN** 系统生成验证码
- **THEN** 验证码存储到 Redis，Key 为 `yun-picture:email:code:{email}`
- **AND** TTL 设置为 300 秒
- **AND** 5 分钟后验证码自动过期

#### Scenario: New code overwrites old code

- **WHEN** 同一邮箱重新请求验证码（已过防重发冷却期）
- **THEN** 新验证码覆盖 Redis 中的旧验证码
- **AND** 旧验证码立即失效

### Requirement: Email Verification Code Sending

系统 SHALL 通过 SMTP 发送验证码邮件到用户邮箱。

邮件 SHALL 包含以下信息：
- 验证码（6 位数字）
- 有效期说明（5 分钟）
- 安全提示（勿泄露他人）

#### Scenario: Send verification code email successfully

- **WHEN** 用户请求发送验证码
- **AND** 邮箱地址存在于系统中
- **AND** 未在防重发冷却期内
- **THEN** 系统生成验证码
- **AND** 发送邮件到用户邮箱
- **AND** 邮件包含验证码、有效期和安全提示
- **AND** 返回成功响应

#### Scenario: Email sending failure handling

- **WHEN** SMTP 服务不可用或配置错误
- **THEN** 系统返回错误："验证码发送失败，请稍后重试"
- **AND** 不写入 Redis
- **AND** 不写入防重发记录

### Requirement: Anti-Spam Protection

系统 SHALL 限制同一邮箱 60 秒内只能发送一次验证码，防止滥用。

防重发机制：
- Key 格式：`yun-picture:email:cooldown:{email}`
- Value：标记值（如 "1"）
- TTL：60 秒
- 发送成功后立即写入

#### Scenario: Prevent sending code within 60 seconds

- **WHEN** 用户请求发送验证码
- **AND** Redis 中存在 `yun-picture:email:cooldown:{email}` 记录
- **THEN** 系统返回错误："发送过于频繁，请 60 秒后重试"
- **AND** 不生成新验证码
- **AND** 不发送邮件

#### Scenario: Allow sending after cooldown period

- **WHEN** 用户请求发送验证码
- **AND** Redis 中不存在 `yun-picture:email:cooldown:{email}` 记录（已过 60 秒）
- **THEN** 系统生成并发送验证码
- **AND** 写入新的 cooldown 记录（TTL 60 秒）

### Requirement: Single-Use Code Enforcement

系统 SHALL 确保验证码仅能使用一次，验证成功后立即失效。

单次使用机制：
- 验证成功后写入 `yun-picture:email:used:{email}:{code}` 标记
- TTL：与验证码同步（5 分钟）
- 再次使用时检查此标记

#### Scenario: Code is valid on first use

- **WHEN** 用户首次使用验证码
- **AND** 验证码正确且未过期
- **AND** Redis 中不存在 `yun-picture:email:used:{email}:{code}` 标记
- **THEN** 验证成功
- **AND** 系统写入 `yun-picture:email:used:{email}:{code}` 标记（TTL 5 分钟）

#### Scenario: Code is rejected on second use

- **WHEN** 用户再次使用相同验证码
- **AND** Redis 中存在 `yun-picture:email:used:{email}:{code}` 标记
- **THEN** 系统返回错误："验证码已使用，请重新获取"
- **AND** 不执行后续操作

### Requirement: Email Verification Code Validation

系统 SHALL 验证用户提交的验证码是否正确、未过期、未使用。

验证流程：
1. 检查 `yun-picture:email:used:{email}:{code}` 是否存在（已使用检查）
2. 从 Redis 获取 `yun-picture:email:code:{email}`（过期检查）
3. 对比验证码值（正确性检查）

#### Scenario: Validate code successfully

- **WHEN** 用户提交验证码
- **AND** Redis 中不存在 `yun-picture:email:used:{email}:{code}` 标记
- **AND** Redis 中存在 `yun-picture:email:code:{email}` 且值匹配
- **THEN** 验证成功
- **AND** 返回 true

#### Scenario: Reject expired code

- **WHEN** 用户提交验证码
- **AND** Redis 中不存在 `yun-picture:email:code:{email}`（已过期或不存在）
- **THEN** 验证失败
- **AND** 返回错误："验证码已过期或无效"

#### Scenario: Reject incorrect code

- **WHEN** 用户提交验证码
- **AND** Redis 中存在 `yun-picture:email:code:{email}` 但值不匹配
- **THEN** 验证失败
- **AND** 返回错误："验证码错误"

#### Scenario: Reject used code

- **WHEN** 用户提交验证码
- **AND** Redis 中存在 `yun-picture:email:used:{email}:{code}` 标记
- **THEN** 验证失败
- **AND** 返回错误："验证码已使用，请重新获取"

### Requirement: Email Login with Verification Code

系统 SHALL 支持用户通过邮箱和验证码登录，作为账号密码登录的补充方式。

登录流程：
1. 用户请求发送验证码（codeType: LOGIN）
2. 系统校验邮箱是否存在
3. 发送验证码邮件
4. 用户提交邮箱和验证码
5. 系统验证码校验
6. 创建用户 Session
7. 返回 LoginUserVO

#### Scenario: Email login successfully

- **WHEN** 用户请求邮箱验证码登录
- **AND** 邮箱存在于系统中
- **AND** 验证码正确、未过期、未使用
- **THEN** 系统验证成功
- **AND** 标记验证码为已使用
- **AND** 创建用户 Session（与账号密码登录行为一致）
- **AND** 返回 LoginUserVO（包含 id, userName, userAvatar, userRole 等）

#### Scenario: Email not found on login

- **WHEN** 用户请求发送登录验证码
- **AND** 邮箱不存在于系统中
- **THEN** 系统返回错误："邮箱不存在"
- **AND** 不发送验证码

#### Scenario: Invalid code on login

- **WHEN** 用户提交邮箱和验证码进行登录
- **AND** 验证码错误、已过期或已使用
- **THEN** 系统返回相应错误
- **AND** 不创建 Session

### Requirement: Email Password Reset with Verification Code

系统 SHALL 支持用户通过邮箱验证码重置密码（忘记密码场景，无需旧密码）。

重置流程：
1. 用户请求发送验证码（codeType: RESET_PASSWORD）
2. 系统校验邮箱是否存在
3. 发送验证码邮件
4. 用户提交邮箱、验证码、新密码、确认密码
5. 系统验证码校验
6. 更新密码
7. 返回成功

#### Scenario: Reset password successfully

- **WHEN** 用户请求通过邮箱验证码重置密码
- **AND** 邮箱存在于系统中
- **AND** 验证码正确、未过期、未使用
- **AND** 新密码与确认密码一致
- **AND** 新密码符合格式要求
- **THEN** 系统验证成功
- **AND** 标记验证码为已使用
- **AND** 加密新密码（与注册时加密方式一致）
- **AND** 更新数据库 user.userPassword 字段
- **AND** 返回成功响应

#### Scenario: Password mismatch on reset

- **WHEN** 用户提交重置密码请求
- **AND** 新密码与确认密码不一致
- **THEN** 系统返回错误："两次密码输入不一致"
- **AND** 不更新数据库
- **AND** 不标记验证码为已使用

#### Scenario: Email not found on reset

- **WHEN** 用户请求发送重置密码验证码
- **AND** 邮箱不存在于系统中
- **THEN** 系统返回错误："邮箱不存在"
- **AND** 不发送验证码

### Requirement: Email Uniqueness Constraint

系统 SHALL 确保 User.email 字段唯一（允许为空），支持通过邮箱直接查询用户。

数据约束：
- User.email 可以为空（NULL）
- 如果 email 有值，则必须唯一（UNIQUE KEY）
- 支持通过 email 字段精确查询用户

#### Scenario: Email uniqueness enforced in database

- **WHEN** 尝试插入或更新用户记录
- **AND** email 值不为 NULL 且与现有记录重复
- **THEN** 数据库返回唯一性约束冲突错误
- **AND** 操作失败

#### Scenario: Query user by email

- **WHEN** 系统需要根据邮箱查询用户（登录/重置密码）
- **THEN** 系统通过 `SELECT * FROM user WHERE email = ?` 查询
- **AND** 最多返回一条记录（因唯一性约束）
- **AND** 如果不存在则返回 null

#### Scenario: Multiple NULL emails allowed

- **WHEN** 多个用户的 email 字段为 NULL
- **THEN** 数据库允许这些记录存在（MySQL UNIQUE 约束允许多个 NULL）
- **AND** 不触发唯一性约束冲突

### Requirement: API Endpoints

系统 SHALL 提供以下 REST API 端点支持邮箱验证码功能。

端点规范：
- 基础路径：`/api/user/email/`
- 请求格式：JSON
- 响应格式：统一 BaseResponse 包装

#### Scenario: Send verification code endpoint

- **GIVEN** 端点路径为 `POST /api/user/email/sendCode`
- **WHEN** 客户端发送请求
  ```json
  {
    "email": "user@example.com",
    "codeType": "LOGIN"  // 或 "RESET_PASSWORD"
  }
  ```
- **THEN** 系统返回
  ```json
  {
    "code": 0,
    "message": "验证码已发送，请查收邮件",
    "data": 
  }
  ```

#### Scenario: Email login endpoint

- **GIVEN** 端点路径为 `POST /api/user/email/login`
- **WHEN** 客户端发送请求
  ```json
  {
    "email": "user@example.com",
    "code": "123456"
  }
  ```
- **THEN** 系统返回
  ```json
  {
    "code": 0,
    "message": "ok",
    "data": {
      "id": 1,
      "userName": "张三",
      "userAvatar": "https://...",
      "userRole": "user"
    }
  }
  ```
- **AND** 创建用户 Session

#### Scenario: Reset password endpoint

- **GIVEN** 端点路径为 `POST /api/user/email/resetPassword`
- **WHEN** 客户端发送请求
  ```json
  {
    "email": "user@example.com",
    "code": "123456",
    "newPassword": "newpass123",
    "checkPassword": "newpass123"
  }
  ```
- **THEN** 系统返回
  ```json
  {
    "code": 0,
    "message": "密码重置成功",
    "data": true
  }
  ```

### Requirement: Error Handling

系统 SHALL 为验证码相关错误提供明确的错误码和提示信息。

错误场景处理：
- 验证码错误：清晰提示"验证码错误"
- 验证码过期：清晰提示"验证码已过期或无效"
- 验证码已使用：清晰提示"验证码已使用，请重新获取"
- 发送过于频繁：清晰提示"发送过于频繁，请 60 秒后重试"
- 邮箱不存在：清晰提示"邮箱不存在"
- 验证码服务发送失败：清晰提示"当前邮箱验证服务不可用，请稍后重试，或联系系统管理员"

#### Scenario: Return clear error messages

- **WHEN** 验证码校验失败（任何原因）
- **THEN** 系统返回
  ```json
  {
    "code": 40000,  // 参数错误
    "message": "[具体错误原因]",
    "data": null
  }
  ```
- **AND** message 字段包含用户可理解的错误描述

#### Scenario: Handle Redis connection failure gracefully

- **WHEN** Redis 服务不可用
- **THEN** 系统捕获异常
- **AND** 返回错误："验证码服务暂不可用，请稍后重试"
- **AND** 不影响现有账号密码登录和根据旧密码修改新密码功能（降级方案）
