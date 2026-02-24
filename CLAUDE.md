# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `.spec-workflow/steering/` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `.spec-workflow/steering/` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

## Project Overview

云图床（yun-picture）是一个前后端分离的图片管理平台。这是后端服务，基于 Spring Boot 3.5.7 + Java 17 构建。

```
yun-picture/
├── yun-picture-backend/   # Spring Boot 后端（本目录）
└── yun-picture-frontend/  # Vue 3 前端
```

## Tech Stack

- **Spring Boot 3.5.7** + Java 17
- **MyBatis Plus 3.5.14** - 数据库访问
- **MySQL 8.0+** + **Redis** - 数据存储
- **腾讯云 COS** - 对象存储
- **Google Gemini AI** / **阿里通义千问** - AI 图片生成
- **Knife4j 4.4.0** - API 文档
- **Lombok** - 减少样板代码

## Architecture

标准三层架构 + Manager 层模式：

```
Controller → Service → Mapper (数据库)
               ↓
            Manager (外部服务: COS, AI, 邮件等)
```

### Layer Responsibilities

- **Controller**: HTTP 请求处理、参数验证、返回响应。不包含业务逻辑。
- **Service**: 业务逻辑处理、数据校验、事务管理。唯一可复用的业务层。
- **Manager**: 第三方服务封装（COS、AI、邮件）、复杂业务编排。
- **Mapper**: MyBatis 数据访问层。纯数据访问，不涉及业务判断。

### Package Structure

```
com.wuzhenhua.yunpicturebackend/
├── annotation/         # 自定义注解（@AuthCheck, @VipLevelCheck）
├── aop/                # AOP 切面（权限拦截、VIP 等级检查）
├── api/                # 外部 API 调用（Gemini, 阿里云, 图片搜索）
├── common/             # 通用类（BaseResponse, PageRequest, DeleteRequest）
├── config/             # 配置类（COS, Redis, MyBatis, Knife4j）
├── constant/           # 常量类（用户角色等）
├── controller/         # REST API 控制器
├── exception/          # 异常类（BusinessException, ErrorCode, GlobalExceptionHandler）
├── manager/            # Manager 层（CosManager, FileManager, PictureUpload）
├── mapper/             # MyBatis Mapper 接口
├── model/
│   ├── dto/            # 数据传输对象（按模块分包：user/, picture/, space/）
│   ├── entity/         # 数据库实体类
│   ├── enums/          # 枚举类
│   └── vo/             # 视图对象
├── service/            # Service 接口
│   └── impl/           # Service 实现
└── utils/              # 工具类（ResultUtils, ThrowUtils）
```

## Common Commands

### Development

**启动后端**:
```bash
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```

**构建**:
```bash
./mvnw clean package
```

**测试**:
```bash
# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=UserServiceTest

# 运行单个测试方法
./mvnw test -Dtest=UserServiceTest#testUserRegister
```

### API Documentation

- **Knife4j**: http://localhost:8081/api/doc.html
- **OpenAPI Spec**: http://localhost:8081/api/v3/api-docs/default

## API Documentation

项目使用 **Knife4j** 注解定义 API。所有 Controller 方法必须添加：
- `@Tag` - 类级别的 API 分组
- `@Operation` - 方法级别的 API 描述
- `@Parameter` - 参数说明（可选）
- `@Schema` - DTO 字段说明

```java
@Tag(name = "UserController", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // ...
    }
}
```

**重要**：修改后端 API 后，前端需要运行 `npm run openapi` 重新生成类型安全的 API 客户端。

## Error Handling

### Error Code Convention

所有错误通过 `ErrorCode` 枚举定义：
```java
public enum ErrorCode {
    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");
}
```

### Throwing Exceptions

使用 `ThrowUtils` 进行条件校验和异常抛出：
```java
ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
ThrowUtils.throwIf(userRole != UserRoleEnum.ADMIN, ErrorCode.NO_AUTH_ERROR);
```

### Global Exception Handler

`GlobalExceptionHandler` 自动捕获所有异常并返回统一格式 `BaseResponse`：
- `BusinessException`: 返回自定义错误码和消息
- `RuntimeException`: 返回系统错误（50000）

## Authentication & Authorization

### Authentication

使用 **Spring Session + Redis** 实现会话管理。
- Session 超时时间：30 天
- 获取登录用户：`userService.getLoginUser(request)`

### Authorization

使用 **AOP + 自定义注解** 实现权限控制：

```java
@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
public BaseResponse<Long> addUser(@RequestBody UserAddRequest request) {
    // 只有管理员可以访问
}
```

支持的注解：
- `@AuthCheck`: 角色权限校验
- `@VipLevelCheck`: VIP 等级校验

## Database Access

### MyBatis Plus Configuration

- **逻辑删除**: `isDeleted` 字段（1=已删除，0=未删除）
- **自动驼峰转换**: 下划线命名 → 驼峰命名
- **主键策略**: 雪花算法（`IdType.ASSIGN_ID`）

### Entity Example

```java
@TableName(value = "user")
@Data
public class User implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @TableLogic
    private Integer isDeleted;

    private String userAccount;
    // ... 其他字段
}
```

### Mapper Pattern

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus 自动提供 CRUD 方法
    // 如需自定义 SQL，在 resources/mapper/UserMapper.xml 中编写
}
```

### Service Pattern

```java
public interface UserService extends IService<User> {
    User getLoginUser(HttpServletRequest request);
}

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    // 实现业务方法
}
```

## Third-Party Integration

### Tencent Cloud COS

通过 `CosManager` 封装：
- `putObject()` - 上传普通对象
- `putPictureObject()` - 上传图片（自动压缩、生成缩略图）
- `putUrlPictureObject()` - 上传 URL 图片（自动压缩）
- `deleteObject()` - 删除对象

**图片处理规则**：
- 自动生成 WebP 格式压缩版本
- 大于 20KB 的图片自动生成 256x256 缩略图

### AI Integration

- **Google Gemini**: AI 图片生成、图片聊天
- **阿里通义千问**: 扩图（OutPainting）功能

通过 `api/` 包下的封装类调用外部 AI 服务。

### Email Service

使用 Spring Boot `JavaMailSender` 发送邮件：
- 邮箱验证码
- 密码重置链接

## Configuration

### Profiles

项目支持多环境配置：
- `application.yml` - 主配置
- `application-local.yml` - 本地开发（默认激活）
- `application-dev.yml` - 开发环境
- `application-prod.yml` - 生产环境

激活配置：
```yaml
spring:
  profiles:
    active: local
```

### Environment Variables

敏感配置通过环境变量或配置文件设置：
- `spring.datasource.*` - 数据库配置
- `cos.client.*` - 腾讯云 COS 配置
- `spring.data.redis.*` - Redis 配置
- `spring.mail.*` - 邮件配置
- `langchain4j.google-ai-gemini.*` - Gemini AI 配置
- `aliYunAi.apiKey` - 阿里云 AI 配置

**警告**：配置文件中的敏感信息已被注释，实际运行时需要填写或通过环境变量配置。

### Server Configuration

- **端口**: 8081
- **Context Path**: `/api`
- **Max HTTP Request Header Size**: 1024KB
- **Session Timeout**: 30 天
- **Max File Size**: 10MB
- **Max Request Size**: 100MB

## Model Layer Patterns

### DTO (Data Transfer Object)

按模块分包组织：
```
model/dto/
├── user/
│   ├── UserRegisterRequest.java
│   ├── UserLoginRequest.java
│   ├── UserUpdateRequest.java
│   └── ...
├── picture/
│   ├── PictureUploadRequest.java
│   ├── PictureQueryRequest.java
│   └── ...
└── space/
    ├── SpaceAddRequest.java
    └── ...
```

命名约定：`{Entity}{Action}Request` 或 `{Entity}{Attribute}Request`

### VO (View Object)

返回给前端的数据（脱敏、简化）：
- `UserVO` - 用户公开信息
- `LoginUserVO` - 登录用户信息
- `PictureVO` - 图片信息
- `SpaceVO` - 空间信息

### Enum

所有枚举类放在 `model/enums/`：
- `UserRoleEnum` - 用户角色（ADMIN, USER）
- `UserVIPLevelEnum` - VIP 等级
- `SpaceLevelEnum` - 空间等级
- `PictureReviewStatusEnum` - 图片审核状态

## Coding Standards

### Naming Conventions

| 类型 | 命名约定 | 示例 |
|-----|---------|------|
| Entity | `{Name}` | `User`, `Picture` |
| Mapper | `{Name}Mapper` | `UserMapper` |
| Service | `{Name}Service` | `UserService` |
| ServiceImpl | `{Name}ServiceImpl` | `UserServiceImpl` |
| Controller | `{Name}Controller` | `UserController` |
| DTO Request | `{Name}{Action}Request` | `UserRegisterRequest` |
| VO | `{Name}VO` | `UserVO` |
| Util | `{Name}Utils` | `StringUtils` |

### Lombok Usage

大量使用 Lombok 注解减少样板代码：
- `@Data` - 生成 getter/setter/toString/equals/hashCode
- `@Slf4j` - 日志对象
- `@AllArgsConstructor` / `@NoArgsConstructor` - 构造函数

### Response Format

所有 API 返回统一格式：
```java
{
    "code": 0,           // 状态码
    "data": {...},       // 响应数据
    "message": "ok"      // 响应消息
}
```

使用工具类快速构造：
```java
ResultUtils.success(data);              // 成功响应
ResultUtils.error(ErrorCode.PARAMS_ERROR);  // 错误响应
ResultUtils.error(ErrorCode.NOT_FOUND_ERROR, "用户不存在");  // 自定义消息
```

## Testing

### Test Structure

```
src/test/java/com/wuzhenhua/yunpicturebackend/
└── YuPictureBackendApplicationTests.java  # 主测试类
```

### Running Tests

```bash
# 运行所有测试
./mvnw test

# 跳过测试构建
./mvnw clean package -DskipTests
```

### Test Guidelines

1. 为 Service 层编写单元测试
2. 为 Controller 层编写集成测试
3. 使用 `@SpringBootTest` 注解
4. 使用 `@MockBean` 模拟依赖

## Windows Specific Issues

### Maven Wrapper

Windows 上使用 `mvnw.cmd` 而不是 `./mvnw`：
```cmd
mvnw.cmd spring-boot:run
mvnw.cmd clean package
```

### Port Issues (EACCES permission denied)

如果遇到端口占用错误：
```cmd
net stop winnat
net start winnat
```

如果无效：
1. 检查端口占用：`netstat -ano | findstr :8081`
2. 检查 Hyper-V 端口保留：`netsh interface ipv4 show excludedportrange protocol=tcp`
3. 以管理员权限运行终端

## Module Addition Workflow

添加新模块（例如"标签"管理）的步骤：

1. 创建 Entity: `model/entity/Tag.java`
2. 创建 Mapper: `mapper/TagMapper.java`
3. 创建 Service 接口和实现: `service/TagService.java` + `service/impl/TagServiceImpl.java`
4. 创建 Controller: `controller/TagController.java`
5. 创建 DTO: `model/dto/tag/TagAddRequest.java` 等
6. 创建 VO: `model/vo/TagVO.java`
7. 添加 Knife4j 注解
8. 编写单元测试（可选）

## Notes for AI Assistants

- **API 契约驱动**：修改后端 API 后，提醒前端运行 `npm run openapi` 重新生成客户端
- **分层清晰**：Controller 只做路由，Service 做业务，Manager 做外部服务调用
- **统一异常处理**：使用 `BusinessException` + `GlobalExceptionHandler`
- **逻辑删除**：所有删除操作使用 MyBatis Plus 的逻辑删除
- **权限校验**：使用 `@AuthCheck` 注解，不要在 Controller 中写权限逻辑
- **参数校验**：使用 `ThrowUtils` 进行前置校验
- **响应格式**：统一使用 `BaseResponse` 和 `ResultUtils`
