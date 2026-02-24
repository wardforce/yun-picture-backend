# 狐仙云图后端 (yun-picture-backend)

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)
![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.14-blue)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

一个基于 Spring Boot 的现代化图床管理系统后端服务

</div>

## 项目简介

狐仙云图是一个前后端分离的图片管理平台，提供图片上传、存储、管理、AI 生成和空间管理等功能。

### 核心特性

- **图片管理**：支持本地文件和 URL 上传、批量操作、编辑、删除
- **AI 功能**：集成 Google Gemini 和阿里通义千问，支持 AI 图片生成、扩图、以图搜图
- **空间管理**：支持私人空间和公共空间，不同空间等级和配额管理
- **用户权限**：基于角色的权限控制（管理员/普通用户）和 VIP 等级系统
- **对象存储**：集成腾讯云 COS，支持图片压缩和缩略图生成
- **缓存系统**：本地缓存（Caffeine）+ Redis 分布式缓存
- **审核机制**：图片审核工作流
- **邮箱服务**：邮箱验证码登录和密码重置
- **以图搜图**：基于颜色和图片内容的搜索

## 技术栈

### 后端框架
- **Spring Boot 3.5.7** - 核心框架
- **Spring Session + Redis** - 会话管理
- **MyBatis Plus 3.5.14** - 数据库 ORM
- **MySQL 8.0+** - 关系型数据库
- **Redis** - 缓存和会话存储

### 工具库
- **Hutool 5.8.40** - Java 工具库
- **Lombok** - 减少样板代码
- **Caffeine 3.2.0** - 本地高性能缓存
- **OkHttp 4.12.0** - HTTP 客户端
- **Jsoup 1.21.2** - HTML 解析

### 第三方服务
- **腾讯云 COS** - 对象存储
- **Google Gemini AI** - AI 图片生成
- **阿里通义千问** - AI 扩图功能
- **So Image Search** - 以图搜图

### 文档工具
- **Knife4j 4.4.0** - API 文档（Swagger 增强版）
- **Springdoc OpenAPI 2.7.0** - OpenAPI 规范生成

## 项目结构

```
yun-picture-backend/
├── src/main/java/com/wuzhenhua/yunpicturebackend/
│   ├── annotation/              # 自定义注解（@AuthCheck, @VipLevelCheck）
│   ├── aop/                   # AOP 切面（权限拦截、VIP 等级检查）
│   ├── api/                   # 外部 API 调用
│   │   ├── gemini/           # Google Gemini AI
│   │   ├── aliyun/           # 阿里云 AI
│   │   └── imagesearch/      # 图片搜索
│   ├── common/                # 通用类（BaseResponse, PageRequest 等）
│   ├── config/                # 配置类（COS, Redis, MyBatis, Knife4j）
│   ├── constant/              # 常量类（用户角色等）
│   ├── controller/            # REST API 控制器
│   │   ├── UserController.java
│   │   ├── PictureController.java
│   │   ├── SpaceController.java
│   │   ├── AiPictureGeneratorController.java
│   │   └── ...
│   ├── exception/             # 异常处理
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   ├── manager/               # Manager 层（外部服务封装）
│   │   ├── CosManager.java
│   │   ├── FileManager.java
│   │   └── upload/          # 图片上传策略
│   ├── mapper/               # MyBatis Mapper 接口
│   ├── model/
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 数据库实体类
│   │   ├── enums/           # 枚举类
│   │   └── vo/             # 视图对象
│   ├── service/             # Service 接口和实现
│   ├── utils/               # 工具类
│   └── YunPictureBackendApplication.java
└── src/main/resources/
    ├── application.yml       # 主配置
    ├── application-local.yml # 本地开发配置
    ├── application-dev.yml   # 开发环境配置
    └── application-prod.yml # 生产环境配置
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis

### 配置说明

在运行项目前，需要配置以下敏感信息（在 `application-local.yml` 或环境变量中）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/yun_picture
    username: your_username
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379

cos:
  client:
    host: your_cos_host
    secretId: your_secret_id
    secretKey: your_secret_key
    region: your_region
    bucket: your_bucket

spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email
    password: your_password

langchain4j:
  google-ai-gemini:
    chat-model:
      base-url: your_gemini_url
      api-key: your_gemini_api_key
      model-name: gemini-3-pro-image

aliYunAi:
  apiKey: your_aliyun_api_key
```

### 安装和运行

1. **克隆项目**
```bash
git clone https://github.com/wardforce/yun-picture-backend.git
cd yun-picture-backend
```

2. **配置数据库**
创建数据库并执行 SQL 脚本（如有）：
```sql
CREATE DATABASE yun_picture CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **修改配置**
编辑 `src/main/resources/application-local.yml`，填入你的配置信息。

4. **启动服务**
```bash
# Unix/Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

5. **访问 API 文档**
服务启动后，访问：
- **Knife4j**: http://localhost:8081/api/doc.html
- **OpenAPI**: http://localhost:8081/api/v3/api-docs/default

### 构建项目

```bash
./mvnw clean package
```

生成的 JAR 文件位于 `target/yun-picture-backend-0.0.1-SNAPSHOT.jar`。

### 运行测试

```bash
./mvnw test
```

## API 文档

项目使用 **Knife4j** 提供在线 API 文档，包含：
- 接口分组和描述
- 请求参数说明
- 响应示例
- 在线调试功能

### 主要接口

| 模块 | 路径前缀 | 说明 |
|-----|---------|------|
| 用户管理 | `/user` | 注册、登录、信息管理 |
| 图片管理 | `/picture` | 上传、查询、编辑、删除 |
| 空间管理 | `/space` | 空间创建、管理、分析 |
| AI 生成 | `/ai/generator` | AI 图片生成 |
| 文件管理 | `/file` | 文件上传、头像管理 |
| 聊天历史 | `/chat` | AI 聊天会话管理 |

## 核心功能

### 1. 图片管理

- **上传**：支持本地文件和 URL 上传
- **批量操作**：批量上传、编辑
- **图片处理**：自动 WebP 压缩、缩略图生成
- **搜索**：按颜色、标签、分类搜索
- **以图搜图**：基于图片内容的相似度搜索

### 2. AI 功能

- **图片生成**：通过 Google Gemini 生成 AI 图片
- **扩图**：使用阿里通义千问进行图片扩展
- **图片聊天**：基于图片的 AI 对话

### 3. 空间管理

- **空间类型**：私人空间、公共空间
- **配额管理**：不同等级的空间有不同的存储上限
- **权限控制**：空间访问权限校验

### 4. 用户系统

- **角色系统**：管理员、普通用户
- **VIP 等级**：支持多级 VIP，不同等级享受不同权益
- **邮箱验证**：邮箱验证码登录
- **密码重置**：通过邮箱重置密码

### 5. 审核机制

- **审核流程**：图片上传后需管理员审核
- **审核状态**：待审核、审核通过、审核拒绝

## 架构设计

### 分层架构

```
Controller → Service → Mapper (数据库)
               ↓
            Manager (外部服务: COS, AI, 邮件等)
```

### 职责划分

| 层级 | 职责 |
|-----|------|
| **Controller** | HTTP 请求处理、参数验证、返回响应 |
| **Service** | 业务逻辑处理、数据校验、事务管理 |
| **Manager** | 第三方服务封装（COS、AI、邮件） |
| **Mapper** | MyBatis 数据访问层，纯数据访问 |

### 安全机制

- **认证**：Spring Session + Redis 会话管理
- **授权**：AOP + 自定义注解（@AuthCheck, @VipLevelCheck）
- **数据校验**：统一异常处理和错误码规范

### 缓存策略

- **本地缓存（Caffeine）**：高频访问数据，5 分钟过期
- **分布式缓存（Redis）**：会话存储、图片列表缓存

## 开发指南

### 添加新模块

1. 创建 Entity：`model/entity/YourEntity.java`
2. 创建 Mapper：`mapper/YourEntityMapper.java`
3. 创建 Service 接口和实现：`service/YourEntityService.java` + `service/impl/YourEntityServiceImpl.java`
4. 创建 Controller：`controller/YourEntityController.java`
5. 创建 DTO：`model/dto/yourmodule/YourEntityAddRequest.java` 等
6. 创建 VO：`model/vo/YourEntityVO.java`
7. 添加 Knife4j 注解

### 代码规范

- **命名约定**：
  - Entity: `{Name}`
  - Mapper: `{Name}Mapper`
  - Service: `{Name}Service`
  - Controller: `{Name}Controller`
  - DTO Request: `{Name}{Action}Request`
  - VO: `{Name}VO`

- **Lombok 注解**：广泛使用 `@Data`、`@Slf4j`、`@AllArgsConstructor` 等减少样板代码

- **异常处理**：使用 `BusinessException` + `GlobalExceptionHandler` 统一处理异常

- **权限校验**：使用 `@AuthCheck` 注解，不要在 Controller 中写权限逻辑

## 部署

### 生产环境配置

1. 修改 `application-prod.yml` 中的生产环境配置
2. 激活生产环境 profile：
```yaml
spring:
  profiles:
    active: prod
```

3. 构建并打包：
```bash
./mvnw clean package -DskipTests
```

4. 运行 JAR 文件：
```bash
java -jar target/yun-picture-backend-0.0.1-SNAPSHOT.jar
```

## 常见问题

### Windows 端口占用错误

如果遇到 `EACCES permission denied` 错误：

```cmd
net stop winnat
net start winnat
```

### 配置敏感信息

生产环境请勿在配置文件中硬编码敏感信息，建议：
- 使用环境变量
- 使用配置中心（如 Nacos、Apollo）
- 使用密钥管理服务（如 AWS Secrets Manager）

## 前端项目

狐仙云图前端项目地址：https://github.com/wardforce/yun-picture-frontend

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

## 贡献

欢迎提交 Issue 和 Pull Request！

---

<div align="center">

**狐仙云图** - 让图片管理更简单

Made with ❤️ by wardforce

</div>
