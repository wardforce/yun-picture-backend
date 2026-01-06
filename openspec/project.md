# 项目上下文

## 项目目的

云图（Yun Picture）是一个云端图片管理平台，为用户提供图片上传、管理、组织和搜索功能。系统支持高级特性如按颜色搜索、相似度搜索（反向图搜）、批量操作和基于空间的组织管理，同时提供角色的访问控制。

## 技术栈

- **编程语言**: Java 17
- **框架**: Spring Boot 3.5.7
- **Web 框架**: Spring Web / Spring MVC
- **数据访问**: MyBatis Plus 3.5.14 (ORM)
- **数据库**: MySQL 8.0+ (jdbc: mysql-connector-java)
- **缓存**: Redis (Spring Data Redis)
- **本地缓存**: Caffeine
- **对象存储**: 腾讯云 COS
- **工具库**: Hutool 5.8.40 (多功能工具库)
- **文档**: SpringDoc OpenAPI 3.0 (Swagger)
- **异步**: Spring @EnableAsync
- **构建工具**: Maven 3.x
- **运行环境**: JDK 17+

## 项目约定

### 代码风格

- **包结构**: `com.wuzhenhua.yunpicturebackend.[layer]`

  - `controller`: REST API 端点
  - `service`: 业务逻辑接口和实现
  - `mapper`: MyBatis Plus 数据访问层
  - `model`: 域模型、DTO、枚举类
    - `entity`: JPA 数据库实体
    - `dto`: 请求/响应 数据传输对象
    - `vo`: 值对象（用于 API 响应）
    - `enums`: 状态和类型枚举
  - `api`: 外部 API 集成门面
  - `manager`: 业务协调和编排
  - `config`: Spring 配置类
  - `annotation`: 自定义注解
  - `aop`: 面向切面编程拦截器
  - `exception`: 自定义异常
  - `utils`: 工具函数
  - `constant`: 应用常量
  - `common`: 通用基类（如 BaseResponse）

- **命名约定**:

  - 类名: PascalCase (例: `PictureController`, `PictureService`, `PictureServiceImpl`)
  - 方法名: camelCase
  - 常量: UPPER_SNAKE_CASE
  - 请求 DTO: `[实体名][操作]Request` (例: `PictureUploadRequest`)
  - 响应 VO: `[实体名]VO`
  - 枚举: `[实体名][类型]Enum` (例: `PictureReviewStatusEnum`)

- **Controller 参数封装规范**:
  - **强制封装**: 除以下例外情况外,所有业务参数必须封装到 Request/Response/VO 对象中
  - **例外情况**:
    - 只有单个参数的简单接口
    - 框架级参数: `MultipartFile`, `HttpServletRequest`, `HttpServletResponse` 等
    - 路径变量: `@PathVariable` 标注的参数
  - **禁止**: 在 Controller 方法签名中使用多个 `@RequestParam` 或 `@RequestBody` 字段
  - **示例**:

    ```java
    // ✅ 正确: 参数封装到 Request 对象
    @PostMapping("/generate_ai_image")
    public BaseResponse<AiGenerateResponse> generateAiImage(
            @RequestBody CreateChatRequest request,
            HttpServletRequest httpServletRequest) { ... }
    
    // ❌ 错误: 多个散装参数
    @PostMapping("/generate_ai_image")
    public BaseResponse<AiGenerateResponse> generateAiImage(
            @RequestParam String prompt,
            @RequestParam Long pictureId,
            @RequestParam Long sessionId,
            HttpServletRequest httpServletRequest) { ... }
    ```

- **Serializable 实现规范**:
  - **强制实现**: 所有 Request/Response/VO 类必须实现 `Serializable` 接口
  - **原因**: 支持 Redis 缓存存储、Session 持久化、分布式系统数据传输
  - **版本控制**: 建议显式声明 `serialVersionUID` 以确保序列化兼容性
  - **示例**:

    ```java
    @Data
    public class LoginUserVO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long id;
        private String userName;
        // ... 其他字段
    }
    ```

- **开发工具**:
  - Lombok: 使用 @Data, @Slf4j, @AllArgsConstructor 等注解减少样板代码
  - 异常处理: 自定义 BusinessException 配合 ErrorCode 枚举
  - 日志: SLF4J + Logback (通过 Spring Boot)
  - API 文档: OpenAPI 3.0 注解 (@Tag, @Operation, @ApiResponse)

### 架构模式

- **分层架构**: Controller → Service → Mapper/Manager → Entity
- **DTO 模式**: 所有 REST 端点使用 DTO 来解耦请求/响应
- **Entity-VO 模式**: Entity 映射到 VO 用于 API 响应（使用 BeanUtils.copyProperties）
- **服务门面**: 外部 API 包装在门面类中（例: SoImageSearchApiFacade）
- **Manager 模式**: 业务协调逻辑放在专用 manager 类中
- **注解式安全**: 自定义 @AuthCheck 和 @VipLevelCheck 注解配合 AOP 拦截器
  - AuthInterceptor: 验证用户身份
  - VipLevelInterceptor: 强制执行 VIP 等级限制
- **Redis 缓存**: 使用 StringRedisTemplate 进行集中式缓存管理
- **本地缓存**: Caffeine 用于频繁访问的数据（如标签分类）
- **分页**: MyBatis Plus 的 Page<T> 用于结果分页
- **异步操作**: @EnableAsync 用于非阻塞操作（文件上传、批量处理）

### 测试策略

- **单元测试**: 待定（当前无测试目录；建议使用 JUnit 5 + Mockito）
- **集成测试**: 待定
- **API 测试**: 手工测试或 Postman 集合（待定）

### Git 工作流

- **分支策略**: 从 main 或 develop 创建特性分支
- **提交信息**:
  - 主要语言: **中文**
  - 格式: `[类型]: [描述]` 或祈使句形式
  - 示例:
    - `新增按颜色搜索图片功能`
    - `优化代码和清理未使用的导入`
    - `实现图片相似度查询`
    - `重构图片上传逻辑`
- **PR/合并策略**: 推荐 Squash 或常规提交
- **分支保护**: main 分支应要求 PR 审查

## 业务领域知识

### 核心实体

- **Picture（图片）**: 代表一张图像及其元数据（名称、大小、颜色、尺寸、URL、状态）
- **Space（空间）**: 工作区/文件夹容器，用于组织图片，支持配额管理
- **User（用户）**: 应用用户，包含身份验证、角色和 VIP 等级
- **Tags（标签）**: 图片分类系统，支持父子层级

### 核心业务概念

- **PictureReviewStatus（审核状态）**: 图片审核工作流状态（待审、已批准、已拒绝）
- **UserRole（用户角色）**: 授权级别（普通用户、管理员、VIP）
- **VipLevel（VIP 等级）**: 订阅层级，决定功能访问权限和配额
- **颜色提取**: 从图片提取主要颜色用于搜索优化
- **图片相似度搜索**: 通过 360 搜图 API 进行反向图像搜索

### 数据模型

- 图片存储元数据: url, size, width, height, color（十六进制字符串）, dominant_color
- Space 提供用户隔离和配额管理
- 图片与空间和用户关联
- Redis 缓存高频访问对象，支持 TTL
- Caffeine 本地缓存标签分类

## 重要约束

### 技术约束

- **Java 版本**: 必须 17+ (pom.xml 中配置)
- **Spring Boot 版本**: 3.5.7 (主版本固定)
- **数据库**: MySQL 8.0+ 必需（字符集 UTF-8，时区 Asia/Shanghai）
- **文件上传限制**: 单个文件最大 10MB，整个请求最大 100MB（application.yml 可配）
- **Redis 可用性**: 缓存必需；默认超时 50 秒
- **文件存储**: 生产环境需要腾讯云 COS

### 业务约束

- **身份验证**: 除登录外所有端点都需要有效的用户会话
- **授权**: 根据用户和空间所有权进行资源访问检查
- **VIP 功能**: 某些端点需要特定 VIP 等级（@VipLevelCheck）
- **限流**: 待定（当前未实现；可通过 AOP 添加）
- **配额管理**: 必须强制执行空间级存储配额

### 安全约束

- **会话超时**: 30 天 (2592000 秒)
- **密码**: 已加密（使用 bcrypt 或等效方案；需在 UserService 中验证）
- **CORS**: 已配置（见 CorsConfig.java）
- **API 密钥**: 待定（外部 API 密钥存储在 application.yml 或 .env）

## 外部依赖

### API 集成

- **360 搜图 API** (SoImageSearchApiFacade):

  - 用于反向图像搜索功能
  - 返回: 相似图片列表及其 URL 和元数据
  - 配置: BaseUrl 和凭证（待确认）

- **Google Gemini API** (Gemini.java & AiPictureGeneratorService):
  - 用于 AI 图片生成和多模态图像理解
  - **核心准则**: 严禁在 `Gemini` 基础类中预设全局 `config`；所有调用必须在 Service 层基于业务需要显示构建并传递 `GenerateContentConfig`。
  - 配置: 模型名、API Key、BaseUrl

### 云服务

- **腾讯云 COS** (对象存储):
  - 文件上传和存储
  - 配置: CosClientConfig.java
  - 凭证: 待确认（检查 application.yml 或环境变量）

### 外部库

- **Hutool**: 通用工具库（DateUtil、RandomUtil、JSONUtil 等）
- **Caffeine**: 本地缓存库
- **MyBatis Plus**: ORM 和查询构建器
- **SpringDoc OpenAPI**: 自动生成 Swagger 文档

### 基础设施

- **MySQL**: 主数据存储
- **Redis**: 缓存存储

### 配置文件

- **活跃配置**: `local`（开发环境）
- **其他配置**: dev、prod、staging（待定）
- **服务端口**: 8081（可配置）
- **CORS**: 已启用
