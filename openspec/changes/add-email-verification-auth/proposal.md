# Change: 新增邮箱验证码登录与密码重置功能

## Why

当前用户只能通过 userAccount + password 登录，缺少更便捷的邮箱验证码登录方式。同时用户忘记密码时无法自助重置，影响用户体验。

需要增加：
1. **邮箱验证码登录** - 提供更便捷的免密登录方式
2. **邮箱重置密码** - 用户忘记密码时可通过邮箱验证码自助重置

## What Changes

- **ADDED** 邮箱验证码登录功能（与现有账号密码登录并存）
- **ADDED** 邮箱验证码重置密码功能（忘记密码场景，无需旧密码）
- **ADDED** Redis 验证码管理（生成、存储、校验、防重发）
- **ADDED** 邮件发送服务（发送验证码邮件）
- **MODIFIED** User.email 字段约束（可为空但必须唯一）
- **ADDED** 新 API 端点：
  - `POST /user/email/sendCode` - 发送验证码
  - `POST /user/email/login` - 邮箱验证码登录
  - `POST /user/email/resetPassword` - 邮箱验证码重置密码

**验证码规格**:
- 格式：6位数字（例：123456）
- 存储：Redis，5分钟 TTL
- 防重发：同一邮箱 60 秒内只能发送一次
- 单次使用：验证成功后立即失效，即使未过期

## Impact

- **受影响 specs**: `user-authentication`（新增 capability）
- **受影响代码**:
  - `src/main/java/com/wuzhenhua/yunpicturebackend/controller/UserController.java` - 新增 3 个端点
  - `src/main/java/com/wuzhenhua/yunpicturebackend/service/` - 新增邮件服务和验证码服务
  - `src/main/java/com/wuzhenhua/yunpicturebackend/config/` - 新增 RedisConfig
  - `src/main/java/com/wuzhenhua/yunpicturebackend/model/dto/user/` - 新增 3 个 Request DTO

- **数据库变更**: 为 `user.email` 字段添加 UNIQUE 约束
- **配置变更**: 需要配置 SMTP 服务器信息（用户提供）
- **向后兼容性**: ✅ 兼容 - 现有账号密码登录方式保持不变

## Dependencies

- SMTP 服务器配置（Gmail/163/QQ 等）
- Redis 可用（已配置）
- Spring Boot Mail 依赖（已安装）
