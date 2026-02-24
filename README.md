# 狐仙 AI 云端素材库后端 (Huxian AI Cloud Material Library Backend)

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green)
![MyBatis Plus](https://img.shields.io/badge/MyBatis%20Plus-3.5.14-blue)
![License](https://img.shields.io/badge/License-Apache%202.0-blue)

An innovative enterprise-level intelligent collaborative cloud image library platform backend service based on Spring Boot

</div>

---

## Project Overview

**狐仙 AI 云端素材库** is an innovative enterprise-level intelligent collaborative cloud image library platform focused on providing efficient and convenient cloud-based image material services. The platform integrates cutting-edge **AIGC (AI-Generated Content)** technology, supporting text-to-image generation, image-to-image transformation, image enhancement, public image libraries, private image libraries, and team-shared image libraries.

### English Description

Huxian AI Cloud Material Library is an innovative enterprise-level intelligent collaborative cloud image library platform, focused on providing efficient and convenient cloud image material services. The platform integrates cutting-edge **AIGC (Artificial Intelligence Generated Content)** technology, supporting text-to-image generation, image-to-image generation, image enhancement, public image libraries, private image libraries, and team-shared image libraries.

### 中文描述

狐仙 AI 云端素材库是一款创新的企业级智能协同云图库平台，专注于为提供高效便捷的云端图片素材服务。平台集成了前沿的 AIGC（人工智能生成内容）技术，支持文生图、图生图、图像增强及公共图库、私人图库、团队共享图库等功能。

---

## 核心特性 / Key Features

### 英文 / English

- **Image Management**: Support local file and URL upload, batch operations, editing, and deletion
- **AI Features**: Integration with Google Gemini and Alibaba Tongyi Qianwen, supporting AI image generation, outpainting, and image search
- **Space Management**: Support for private and public spaces with different space levels and quota management
- **User Permissions**: Role-based permission control (Admin/User) and VIP level system
- **Object Storage**: Integration with Tencent Cloud COS, supporting image compression and thumbnail generation
- **Caching System**: Local cache (Caffeine) + Redis distributed cache
- **Review Mechanism**: Image review workflow
- **Email Service**: Email verification code login and password reset
- **Image Search**: Search by color and image content

### 中文 / Chinese

- **图片管理**：支持本地文件和 URL 上传、批量操作、编辑、删除
- **AI 功能**：集成 Google Gemini 和阿里通义千问，支持 AI 图片生成、扩图、以图搜图
- **空间管理**：支持私人空间和公共空间，不同空间等级和配额管理
- **用户权限**：基于角色的权限控制（管理员/普通用户）和 VIP 等级系统
- **对象存储**：集成腾讯云 COS，支持图片压缩和缩略图生成
- **缓存系统**：本地缓存（Caffeine）+ Redis 分布式缓存
- **审核机制**：图片审核工作流
- **邮箱服务**：邮箱验证码登录和密码重置
- **以图搜图**：基于颜色和图片内容的搜索

---

## 技术栈 / Tech Stack

### 后端框架 / Backend Framework

- **Spring Boot 3.5.7** - Core framework
- **Spring Session + Redis** - Session management
- **MyBatis Plus 3.5.14** - Database ORM
- **MySQL 8.0+** - Relational database
- **Redis** - Cache and session storage

### 工具库 / Utility Libraries

- **Hutool 5.8.40** - Java utility library
- **Lombok** - Reduce boilerplate code
- **Caffeine 3.2.0** - Local high-performance cache
- **OkHttp 4.12.0** - HTTP client
- **Jsoup 1.21.2** - HTML parser

### 第三方服务 / Third-party Services

- **Tencent Cloud COS** - Object storage
- **Google Gemini AI** - AI image generation
- **Alibaba Tongyi Qianwen** - AI outpainting functionality
- **So Image Search** - Image search by image

### 文档工具 / Documentation Tools

- **Knife4j 4.4.0** - API documentation (Swagger enhanced)
- **Springdoc OpenAPI 2.7.0** - OpenAPI specification generation

---

## 项目结构 / Project Structure

```
yun-picture-backend/
├── src/main/java/com/wuzhenhua/yunpicturebackend/
│   ├── annotation/              # Custom annotations (@AuthCheck, @VipLevelCheck)
│   ├── aop/                   # AOP aspects (permission interception, VIP level check)
│   ├── api/                   # External API calls
│   │   ├── gemini/           # Google Gemini AI
│   │   ├── aliyun/           # Alibaba Cloud AI
│   │   └── imagesearch/      # Image search
│   ├── common/                # Common classes (BaseResponse, PageRequest, etc.)
│   ├── config/                # Configuration classes (COS, Redis, MyBatis, Knife4j)
│   ├── constant/              # Constant classes (user roles, etc.)
│   ├── controller/            # REST API controllers
│   │   ├── UserController.java
│   │   ├── PictureController.java
│   │   ├── SpaceController.java
│   │   ├── AiPictureGeneratorController.java
│   │   └── ...
│   ├── exception/             # Exception handling
│   │   ├── BusinessException.java
│   │   ├── ErrorCode.java
│   │   └── GlobalExceptionHandler.java
│   ├── manager/               # Manager layer (external service encapsulation)
│   │   ├── CosManager.java
│   │   ├── FileManager.java
│   │   └── upload/          # Image upload strategies
│   ├── mapper/               # MyBatis Mapper interfaces
│   ├── model/
│   │   ├── dto/             # Data transfer objects
│   │   ├── entity/          # Database entity classes
│   │   ├── enums/           # Enum classes
│   │   └── vo/             # View objects
│   ├── service/             # Service interfaces and implementations
│   ├── utils/               # Utility classes
│   └── YunPictureBackendApplication.java
└── src/main/resources/
    ├── application.yml       # Main configuration
    ├── application-local.yml # Local development configuration
    ├── application-dev.yml   # Development environment configuration
    └── application-prod.yml # Production environment configuration
```

---

## 快速开始 / Quick Start

### 环境要求 / Environment Requirements

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis

### 配置说明 / Configuration

Before running the project, configure the following sensitive information (in `application-local.yml` or environment variables):

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

### 安装和运行 / Installation and Running

1. **Clone the project / 克隆项目**
```bash
git clone https://github.com/wardforce/yun-picture-backend.git
cd yun-picture-backend
```

2. **Configure database / 配置数据库**
Create database and execute SQL script (if available):
```sql
CREATE DATABASE yun_picture CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **Modify configuration / 修改配置**
Edit `src/main/resources/application-local.yml` and fill in your configuration information.

4. **Start service / 启动服务**
```bash
# Unix/Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

5. **Access API documentation / 访问 API 文档**
After service starts, access:
- **Knife4j**: http://localhost:8081/api/doc.html
- **OpenAPI**: http://localhost:8081/api/v3/api-docs/default

### 构建项目 / Build Project

```bash
./mvnw clean package
```

The generated JAR file is located at `target/yun-picture-backend-0.0.1-SNAPSHOT.jar`.

### 运行测试 / Run Tests

```bash
./mvnw test
```

---

## API 文档 / API Documentation

The project uses **Knife4j** to provide online API documentation, including:
- Interface groups and descriptions
- Request parameter descriptions
- Response examples
- Online debugging functionality

### 主要接口 / Main APIs

| 模块 / Module | 路径前缀 / Path Prefix | 说明 / Description |
|-----|---------|------|
| 用户管理 / User Management | `/user` | Registration, login, user info management / 注册、登录、信息管理 |
| 图片管理 / Image Management | `/picture` | Upload, query, edit, delete / 上传、查询、编辑、删除 |
| 空间管理 / Space Management | `/space` | Space creation, management, analysis / 空间创建、管理、分析 |
| AI 生成 / AI Generation | `/ai/generator` | AI image generation / AI 图片生成 |
| 文件管理 / File Management | `/file` | File upload, avatar management / 文件上传、头像管理 |
| 聊天历史 / Chat History | `/chat` | AI chat session management / AI 聊天会话管理 |

---

## 核心功能 / Core Features

### 1. 图片管理 / Image Management

- **Upload / 上传**: Support local file and URL upload
- **Batch Operations / 批量操作**: Batch upload and edit
- **Image Processing / 图片处理**: Automatic WebP compression and thumbnail generation
- **Search / 搜索**: Search by color, tags, and categories
- **Image Search by Image / 以图搜图**: Similarity search based on image content

### 2. AI 功能 / AI Features

- **Image Generation / 图片生成**: Generate AI images via Google Gemini
- **Outpainting / 扩图**: Image extension using Alibaba Tongyi Qianwen
- **Image Chat / 图片聊天**: AI dialogue based on images

### 3. 空间管理 / Space Management

- **Space Types / 空间类型**: Private spaces and public spaces
- **Quota Management / 配额管理**: Different space levels have different storage limits
- **Permission Control / 权限控制**: Space access permission verification

### 4. 用户系统 / User System

- **Role System / 角色系统**: Admin and regular users
- **VIP Levels / VIP 等级**: Support for multiple VIP levels with different benefits
- **Email Verification / 邮箱验证**: Email verification code login
- **Password Reset / 密码重置**: Reset password via email

### 5. 审核机制 / Review Mechanism

- **Review Process / 审核流程**: Images require admin review after upload
- **Review Status / 审核状态**: Pending review, approved, rejected

---

## 架构设计 / Architecture Design

### 分层架构 / Layered Architecture

```
Controller → Service → Mapper (Database)
               ↓
            Manager (External Services: COS, AI, Email, etc.)
```

### 职责划分 / Responsibility Division

| 层级 / Layer | 职责 / Responsibility |
|-----|------|
| **Controller** | HTTP request handling, parameter validation, return response |
| **Service** | Business logic processing, data validation, transaction management |
| **Manager** | Third-party service encapsulation (COS, AI, email) |
| **Mapper** | MyBatis data access layer, pure data access |

### 安全机制 / Security Mechanism

- **Authentication / 认证**: Spring Session + Redis session management
- **Authorization / 授权**: AOP + custom annotations (@AuthCheck, @VipLevelCheck)
- **Data Validation / 数据校验**: Unified exception handling and error code specifications

### 缓存策略 / Caching Strategy

- **Local Cache (Caffeine) / 本地缓存**: High-frequency access data, 5 minutes expiration
- **Distributed Cache (Redis) / 分布式缓存**: Session storage, image list cache

---

## 开发指南 / Development Guide

### 添加新模块 / Adding New Modules

1. Create Entity: `model/entity/YourEntity.java`
2. Create Mapper: `mapper/YourEntityMapper.java`
3. Create Service interface and implementation: `service/YourEntityService.java` + `service/impl/YourEntityServiceImpl.java`
4. Create Controller: `controller/YourEntityController.java`
5. Create DTO: `model/dto/yourmodule/YourEntityAddRequest.java` etc.
6. Create VO: `model/vo/YourEntityVO.java`
7. Add Knife4j annotations

### 代码规范 / Code Standards

- **Naming Conventions / 命名约定**:
  - Entity: `{Name}`
  - Mapper: `{Name}Mapper`
  - Service: `{Name}Service`
  - Controller: `{Name}Controller`
  - DTO Request: `{Name}{Action}Request`
  - VO: `{Name}VO`

- **Lombok Annotations / Lombok 注解**: Extensive use of `@Data`, `@Slf4j`, `@AllArgsConstructor` to reduce boilerplate code

- **Exception Handling / 异常处理**: Use `BusinessException` + `GlobalExceptionHandler` for unified exception handling

- **Permission Validation / 权限校验**: Use `@AuthCheck` annotation, don't write permission logic in Controller

---

## 部署 / Deployment

### 生产环境配置 / Production Environment Configuration

1. Modify production environment configuration in `application-prod.yml`
2. Activate production environment profile:
```yaml
spring:
  profiles:
    active: prod
```

3. Build and package:
```bash
./mvnw clean package -DskipTests
```

4. Run JAR file:
```bash
java -jar target/yun-picture-backend-0.0.1-SNAPSHOT.jar
```

---

## 常见问题 / FAQ

### Windows Port Occupancy Error / Windows 端口占用错误

If you encounter `EACCES permission denied` error:

```cmd
net stop winnat
net start winnat
```

### Sensitive Information Configuration / 配置敏感信息

Do not hardcode sensitive information in configuration files in production. Recommended:
- Use environment variables
- Use configuration centers (such as Nacos, Apollo)
- Use key management services (such as AWS Secrets Manager)

---

## 前端项目 / Frontend Project

狐仙 AI 云端素材库前端项目地址：
Huxian AI Cloud Material Library Frontend Project:
https://github.com/wardforce/yun-picture-frontend

---

## 许可证 / License

This project is licensed under the [Apache License 2.0](LICENSE).
本项目采用 [Apache License 2.0](LICENSE) 开源协议。

---

## 贡献 / Contributing

Welcome to submit Issues and Pull Requests!
欢迎提交 Issue 和 Pull Request！

---

<div align="center">

**狐仙 AI 云端素材库 / Huxian AI Cloud Material Library**

Enterprise-Level Intelligent Collaborative Cloud Image Library Platform
创新的企业级智能协同云图库平台

Made with ❤️ by wardforce

</div>
