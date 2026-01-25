# Design: 邮箱验证码登录与密码重置

## Context

当前系统使用 userAccount + password 进行登录，用户信息存储在 MySQL，会话管理通过 Spring Session + Redis。现需新增邮箱验证码登录和密码重置功能，要求：

- 与现有登录方式共存（不破坏现有功能）
- 与现有根据旧密码修改密码方式共存（不破坏现有功能）
- 验证码存储在 Redis，5 分钟有效
- 防止恶意请求（60 秒防重发）
- 确保验证码单次使用

**约束条件**:
- Redis 已配置（127.0.0.1:6379）
- Spring Boot Mail 依赖已安装
- SMTP 已配置
- User.email 字段当前可为空，需改为可空但唯一字段

## Goals / Non-Goals

**Goals**:
- 实现邮箱验证码登录（6位数字，5分钟有效）
- 实现邮箱重置密码（忘记密码场景，无需旧密码）
- 防止验证码滥用（60秒防重发，单次使用）
- 确保 email 唯一性
- 提供清晰的错误提示

**Non-Goals**:
- 不实现 OAuth 第三方登录
- 不实现手机短信验证码
- 不实现邮箱注册功能（保留现有注册方式）
- 不实现图形验证码（后续可扩展）

## Decisions

### 1. Redis Key 设计

**决策**: 使用以下 Key 命名规范

```
验证码存储: yun-picture:email:code:{email} -> "123456"
  TTL: 5 分钟

防重发记录: yun-picture:email:cooldown:{email} -> "1"
  TTL: 60 秒

已使用标记: yun-picture:email:used:{email}:{code} -> "1"
  TTL: 5 分钟（与验证码同步过期）
```

**原因**:
- 前缀分类清晰，便于监控和调试
- 使用 email 作为 Key 一部分，天然去重
- TTL 自动过期，无需手动清理

**替代方案考虑**:
- ❌ 使用 Hash 结构存储所有验证码：增加复杂度，收益不明显
- ❌ 使用 userId 作为 Key：邮箱登录时还没 userId，无法查询

### 2. 验证码生成策略

**决策**: 使用 `RandomUtil.randomNumbers(6)` 生成 6 位纯数字

**原因**:
- 用户易于输入（纯数字）
- 6 位提供 100 万种组合，结合 5 分钟过期和防重发，安全性足够
- Hutool 提供现成工具类，无需额外依赖

**替代方案考虑**:
- ❌ 字母+数字混合：用户输入易错，用户体验差
- ❌ 4 位数字：组合数太少（1万种），安全性不足

### 3. 邮件模板设计

**决策**: 纯文本邮件，包含验证码、有效期、警告语

```
【云图】验证码通知

您的验证码是: 123456

有效期: 5 分钟
请勿将验证码告知他人。

如非本人操作，请忽略此邮件。
```

**原因**:
- 纯文本邮件兼容性最好，不会被邮件客户端拦截
- 内容简洁明了，符合用户预期
- 包含必要的安全提示

**后续优化**:
- 可扩展为 HTML 模板（增加品牌 Logo、更好的排版）

### 4. 防重发实现

**决策**: 两级防护

1. **Redis 防重发记录**: 发送验证码前检查 `yun-picture:email:cooldown:{email}` 是否存在
2. **发送成功后写入**: 设置 60 秒 TTL

**流程**:
```
发送验证码:
1. 检查 yun-picture:email:cooldown:{email} 是否存在
2. 存在 -> 返回 "请勿频繁发送，请60秒后重试"
3. 不存在 -> 生成验证码 -> 发送邮件 -> 写入 Redis
```

**原因**:
- 简单有效，利用 Redis 原子性操作
- 用户体验好（明确告知等待时间）

### 5. 单次使用验证码实现

**决策**: 验证成功后立即写入 `yun-picture:email:used:{email}:{code}` 标记

**流程**:
```
验证验证码:
1. 检查 yun-picture:email:used:{email}:{code} 是否存在
2. 存在 -> 返回 "验证码已使用，请重新获取"
3. 不存在 -> 校验验证码 -> 验证成功 -> 写入 used 标记
```

**原因**:
- 防止重放攻击
- TTL 与验证码同步，自动清理

**替代方案考虑**:
- ❌ 验证成功后删除验证码：会导致并发请求时第二个请求无法判断是"已使用"还是"已过期"
- ✅ 使用 used 标记：能明确区分"已使用"和"已过期"，错误提示更友好

### 6. Email 唯一性约束迁移

**决策**: 添加唯一索引（允许 NULL）

```sql
-- 添加 UNIQUE 约束（MySQL 允许多个 NULL 值存在于 UNIQUE 列）
ALTER TABLE user
ADD UNIQUE KEY uk_email (email);
```

**原因**:
- MySQL 的 UNIQUE 约束允许多个 NULL 值，符合"可为空但唯一"需求
- 无需清理现有 NULL 数据
- 简单直接，风险低

**回滚方案**:
```sql
-- 移除约束
ALTER TABLE user DROP INDEX uk_email;
```

## Component Design

### 新增组件

1. **RedisConfig** (`config/RedisConfig.java`)
   - 配置 StringRedisTemplate Bean
   - 配置 Redis 序列化器

2. **EmailService** (`service/EmailService.java`)
   - `sendVerificationCode(String toEmail, String code)` - 发送验证码邮件
   - 使用 JavaMailSender

3. **VerificationCodeService** (`service/VerificationCodeService.java`)
   - `generateAndSendCode(String email, String codeType)` - 生成并发送验证码
   - `validateCode(String email, String code, String codeType)` - 校验验证码
   - `checkCooldown(String email)` - 检查防重发
   - `markCodeAsUsed(String email, String code)` - 标记验证码已使用

4. **新 DTO 类**:
   - `EmailSendCodeRequest` - 发送验证码请求
   - `EmailLoginRequest` - 邮箱登录请求
   - `EmailResetPasswordRequest` - 邮箱重置密码请求

### 修改组件

1. **UserController** - 新增 3 个端点
2. **UserService** - 新增邮箱登录和重置密码方法
3. **User.java** - email 字段添加 `@Column(unique = true)`（保持可为空）

## Data Flow

### 邮箱登录流程

```
1. 用户请求发送验证码
   POST /user/email/sendCode {"email": "user@example.com", "codeType": "LOGIN"}

2. 后端处理
   a. 检查 email 是否存在于 user 表
   b. 检查 cooldown（60秒防重发）
   c. 生成 6 位验证码
   d. 存储到 Redis: yun-picture:email:code:{email} -> "123456" (TTL 5分钟)
   e. 写入 cooldown: yun-picture:email:cooldown:{email} -> "1" (TTL 60秒)
   f. 发送邮件

3. 用户输入验证码登录
   POST /user/email/login {"email": "user@example.com", "code": "123456"}

4. 后端验证
   a. 检查 yun-picture:email:used:{email}:{code} 是否存在（已使用检查）
   b. 从 Redis 获取 yun-picture:email:code:{email}
   c. 对比验证码
   d. 验证成功 -> 写入 used 标记 -> 查询 User -> 创建 Session -> 返回 LoginUserVO
```

### 重置密码流程

```
1. 用户请求发送验证码
   POST /user/email/sendCode {"email": "user@example.com", "codeType": "RESET_PASSWORD"}

2. 后端处理（同上）

3. 用户提交新密码
   POST /user/email/resetPassword {
     "email": "user@example.com",
     "code": "123456",
     "newPassword": "newpass123",
     "checkPassword": "newpass123"
   }

4. 后端验证
   a. 校验验证码（同登录流程）
   b. 验证成功 -> 加密新密码 -> 更新数据库 -> 返回成功
```

## Risks / Trade-offs

### Risk 1: Email 唯一性约束迁移失败

**风险**: 现有数据库存在 NULL 或重复 email，添加 UNIQUE 约束失败

**缓解措施**:
- 提供迁移 SQL 脚本清理数据
- 在测试环境先验证迁移脚本
- 提供回滚方案

### Risk 2: SMTP 配置错误导致邮件发送失败

**风险**: 用户提供的 SMTP 配置不正确，验证码邮件发送失败

**缓解措施**:
- 在 application.yml 中提供配置模板和注释
- 捕获邮件发送异常，返回明确错误提示
- 建议在测试环境先验证 SMTP 配置

### Risk 3: Redis 不可用导致验证码功能失效

**风险**: Redis 服务异常，无法存储/读取验证码

**缓解措施**:
- 捕获 Redis 异常，返回友好错误提示："验证码服务暂不可用，请稍后重试"
- 现有账号密码登录不受影响（降级方案）

### Trade-off 1: 纯数字验证码 vs 字母数字混合

**选择**: 纯数字验证码

**权衡**:
- ✅ 优点: 用户体验好，易于输入
- ❌ 缺点: 理论安全性略低（100万组合 vs 数千万组合）
- **判断**: 结合 5 分钟过期和防重发，安全性足够

### Trade-off 2: 邮件模板设计

**选择**: 纯文本邮件

**权衡**:
- ✅ 优点: 兼容性好，实现简单，不会被拦截
- ❌ 缺点: 缺少品牌形象，视觉效果一般
- **判断**: 优先保证功能可用，后续可迭代为 HTML 模板

## Migration Plan

### 数据库迁移

**前提条件**: 备份 user 表

```bash
# 1. 备份数据库（在执行迁移前）
mysqldump -u root -p yun_picture user > user_backup_$(date +%Y%m%d).sql

# 2. 执行迁移 SQL（在 MySQL 中手动执行）
# 见 "Decisions - Email 唯一性约束迁移" 部分
```

**验证步骤**:
```sql
-- 检查约束是否生效
SHOW CREATE TABLE user;

-- 验证 email 唯一性
SELECT email, COUNT(*) FROM user GROUP BY email HAVING COUNT(*) > 1;
-- 应返回 0 行
```

**回滚步骤**:
```sql
-- 如果迁移失败，执行回滚
ALTER TABLE user DROP INDEX uk_email;
ALTER TABLE user MODIFY COLUMN email VARCHAR(255) NULL;
```

### 配置迁移

**Step 1**: 配置 SMTP（`application-local.yml`）
1. 我已经做了，不需要你再来配置了


**Step 2**: 重启应用使配置生效

### 部署策略

1. **开发环境测试**: 完整测试所有流程
2. **测试环境验证**: 验证迁移 SQL 和 SMTP 配置
3. **生产环境部署**:
   - 凌晨低峰期执行数据库迁移
   - 部署新代码
   - 监控错误日志和验证码发送成功率

## Open Questions

### Q1: SMTP 服务商选择

**问题**: 使用哪个 SMTP 服务商？

**当前方案**: 我使用了QQ邮箱的SMTP服务

### Q2: 验证码重试次数限制

**问题**: 是否需要限制验证码验证失败次数？

**当前方案**: 无限制，只要在 5 分钟内且未使用即可验证

**风险**: 理论上可以在 5 分钟内暴力尝试所有 6 位数字（100万次）

**建议**: 后续版本可增加"同一邮箱 5 分钟内最多验证失败 5 次"的限制

### Q3: 邮箱验证邮件内容本地化

**问题**: 是否需要支持多语言邮件模板？

**当前方案**: 仅中文

**建议**: 如有国际化需求，后续可根据用户语言偏好发送不同语言邮件

## Testing Strategy

### 单元测试

- `VerificationCodeService.generateAndSendCode()` - 验证码生成和存储
- `VerificationCodeService.validateCode()` - 验证码校验逻辑
- `EmailService.sendVerificationCode()` - 邮件发送（Mock JavaMailSender）

### 集成测试

- 完整流程测试：发送验证码 → 邮箱登录 → 验证 Session
- 边界条件测试：验证码过期、已使用、防重发
- 异常测试：Redis 不可用、邮件发送失败

### 手动测试清单

- [ ] 发送验证码成功，收到邮件
- [ ] 验证码 5 分钟过期
- [ ] 60 秒内不能重复发送
- [ ] 验证码单次使用后失效
- [ ] 邮箱登录成功，创建 Session
- [ ] 重置密码成功，可用新密码登录
- [ ] 不存在的邮箱无法发送验证码
- [ ] 错误的验证码无法登录
