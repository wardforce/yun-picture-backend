# Implementation Tasks

## 1. 配置与基础设施

- [x] 1.1 创建 RedisConfig 配置类
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/config/RedisConfig.java`
  - 内容: 配置 StringRedisTemplate Bean 和序列化器

- [x] 1.2 启用 SMTP 配置
  - 文件: `src/main/resources/application.yml`
  - 内容: 取消注释 spring.mail 配置，填入 SMTP 服务器信息

- [x] 1.3 移除冲突的 JavaMailSender 类
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/utils/JavaMailSender.java`
  - 操作: 删除或重命名此类（与 Spring 内置接口冲突）

## 2. 数据库迁移

- [x] 2.1 清理 User.email 空值和重复数据
  - 执行 SQL: `UPDATE user SET email = CONCAT('placeholder_', id, '@example.com') WHERE email IS NULL OR email = '';`

- [x] 2.2 添加 email 唯一性约束
  - 执行 SQL: `ALTER TABLE user ADD UNIQUE KEY uk_email (email);`


- [x] 2.4 验证约束生效
  - 执行 SQL: `SHOW CREATE TABLE user;`
  - 确认: 包含 UNIQUE KEY `uk_email` 和 NOT NULL 约束

## 3. 创建 DTO 类

- [x] 3.1 创建 EmailSendCodeRequest
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/model/dto/user/EmailSendCodeRequest.java`
  - 字段: `String email`, `String codeType` (LOGIN/RESET_PASSWORD)
  - 实现: `Serializable` 接口

- [x] 3.2 创建 EmailLoginRequest
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/model/dto/user/EmailLoginRequest.java`
  - 字段: `String email`, `String code`
  - 实现: `Serializable` 接口

- [x] 3.3 创建 EmailResetPasswordRequest
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/model/dto/user/EmailResetPasswordRequest.java`
  - 字段: `String email`, `String code`, `String newPassword`, `String checkPassword`
  - 实现: `Serializable` 接口

## 4. 创建核心服务类

- [x] 4.1 创建 EmailService 接口和实现
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/EmailService.java`
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/impl/EmailServiceImpl.java`
  - 方法: `void sendVerificationCode(String toEmail, String code)`
  - 依赖: 注入 JavaMailSender

- [x] 4.2 创建 VerificationCodeService 接口和实现
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/VerificationCodeService.java`
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/impl/VerificationCodeServiceImpl.java`
  - 方法:
    - `void generateAndSendCode(String email, String codeType)` - 生成并发送验证码
    - `boolean validateCode(String email, String code, String codeType)` - 校验验证码
    - `void checkCooldown(String email)` - 检查防重发（抛异常）
    - `void markCodeAsUsed(String email, String code)` - 标记已使用
  - 依赖: 注入 StringRedisTemplate, EmailService

## 5. 扩展 UserService

- [x] 5.1 在 UserService 接口新增方法签名
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/UserService.java`
  - 方法:
    - `LoginUserVO emailLogin(String email, String code, HttpServletRequest request)`
    - `boolean emailResetPassword(String email, String code, String newPassword, String checkPassword)`

- [x] 5.2 在 UserServiceImpl 实现邮箱登录
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/impl/UserServiceImpl.java`
  - 逻辑:
    1. 校验验证码（调用 VerificationCodeService）
    2. 根据 email 查询 User（检查是否存在）
    3. 标记验证码已使用
    4. 创建 Session（与现有 userLogin 逻辑相同）
    5. 返回 LoginUserVO

- [x] 5.3 在 UserServiceImpl 实现邮箱重置密码
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/impl/UserServiceImpl.java`
  - 逻辑:
    1. 参数校验（两次密码一致性）
    2. 校验验证码（调用 VerificationCodeService）
    3. 根据 email 查询 User
    4. 标记验证码已使用
    5. 加密新密码（调用 getEncodePassword）
    6. 更新数据库
    7. 返回成功

## 6. 扩展 UserController

- [x] 6.1 新增发送验证码端点
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/controller/UserController.java`
  - 路由: `POST /user/email/sendCode`
  - 参数: `@RequestBody EmailSendCodeRequest`
  - 逻辑:
    1. 参数校验（email 格式、codeType 枚举）
    2. 根据 codeType 检查用户是否存在（LOGIN 和 RESET_PASSWORD 都需要 email 存在）
    3. 调用 `verificationCodeService.generateAndSendCode()`
    4. 返回成功

- [x] 6.2 新增邮箱登录端点
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/controller/UserController.java`
  - 路由: `POST /user/email/login`
  - 参数: `@RequestBody EmailLoginRequest`, `HttpServletRequest request`
  - 逻辑:
    1. 参数校验
    2. 调用 `userService.emailLogin()`
    3. 返回 LoginUserVO

- [x] 6.3 新增邮箱重置密码端点
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/controller/UserController.java`
  - 路由: `POST /user/email/resetPassword`
  - 参数: `@RequestBody EmailResetPasswordRequest`
  - 逻辑:
    1. 参数校验
    2. 调用 `userService.emailResetPassword()`
    3. 返回成功

- [x] 6.4 为新端点添加 Swagger 文档注解
  - 添加 @Operation, @ApiResponses, @Tag 等注解

## 7. 错误码扩展

- [x] 7.1 在 ErrorCode 枚举中新增错误码（如需要）
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/exception/ErrorCode.java`
  - 说明: 现有 PARAMS_ERROR 和 OPERATION_ERROR 已足够覆盖验证码相关错误

## 8. 编译与验证

- [ ] 8.1 编译项目
  - 命令: `mvn clean compile`
  - 确认: 无编译错误

- [ ] 8.2 启动应用
  - 命令: `mvn spring-boot:run` 或 IDE 启动
  - 确认: 无启动错误，端口 8081 正常监听

## 9. 功能测试

- [ ] 9.1 测试发送验证码（LOGIN）
  - 接口: `POST /api/user/email/sendCode`
  - 请求: `{"email": "test@example.com", "codeType": "LOGIN"}`
  - 预期: 返回成功，邮箱收到验证码

- [ ] 9.2 测试 60 秒防重发
  - 操作: 连续两次调用发送验证码（间隔 < 60秒）
  - 预期: 第二次返回 "发送过于频繁"

- [ ] 9.3 测试邮箱验证码登录
  - 接口: `POST /api/user/email/login`
  - 请求: `{"email": "test@example.com", "code": "123456"}`
  - 预期: 返回 LoginUserVO，Session 创建成功

- [ ] 9.4 测试验证码单次使用
  - 操作: 使用同一验证码登录两次
  - 预期: 第二次返回 "验证码已使用"

- [ ] 9.5 测试验证码过期
  - 操作: 等待 5 分钟后使用验证码
  - 预期: 返回 "验证码已过期或无效"

- [ ] 9.6 测试邮箱重置密码
  - 接口: `POST /api/user/email/resetPassword`
  - 请求: `{"email": "test@example.com", "code": "654321", "newPassword": "newpass123", "checkPassword": "newpass123"}`
  - 预期: 返回成功，可用新密码登录

- [ ] 9.7 测试错误验证码
  - 操作: 使用错误的验证码登录
  - 预期: 返回 "验证码错误"

- [ ] 9.8 测试不存在的邮箱
  - 操作: 向不存在的邮箱发送验证码
  - 预期: 返回 "邮箱不存在"

## 10. 文档更新

- [ ] 10.1 更新 API 文档
  - 访问 Swagger UI: `http://localhost:8081/api/swagger-ui.html`
  - 确认: 新端点显示正确，参数说明清晰

- [ ] 10.2 更新 README（可选）
  - 文件: `README.md`
  - 内容: 新增邮箱验证码登录功能说明
