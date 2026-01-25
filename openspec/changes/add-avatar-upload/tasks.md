# Implementation Tasks

## 1. 创建头像上传服务

- [x] 1.1 创建 AvatarUploadService 接口
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/AvatarUploadService.java`
  - 方法:
    - `String uploadAvatarFromFile(MultipartFile file, Long userId)` - 文件上传
    - `String uploadAvatarFromUrl(String fileUrl, Long userId)` - URL上传
    - `void deleteOldAvatar(String avatarUrl)` - 删除旧头像

- [x] 1.2 创建 AvatarUploadServiceImpl 实现类
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/service/impl/AvatarUploadServiceImpl.java`
  - 核心逻辑:
    - 文件验证（大小2MB，格式jpg/png/webp）
    - 生成COS key: `avatar/{userId}_{timestamp}.{ext}`
    - 使用 `CosManager.putPictureObject()` 上传并压缩为WebP
    - 从URL提取COS key用于删除旧头像
    - 异常处理和容错

## 2. 创建 DTO 类

- [x] 2.1 创建 UploadAvatarRequest
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/model/dto/user/UploadAvatarRequest.java`
  - 字段: `String fileUrl` - 用于URL上传
  - 实现: `Serializable` 接口

## 3. 扩展 UserController

- [x] 3.1 新增独立上传端点: `/user/avatar/upload`
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/controller/UserController.java`
  - 方法: `uploadAvatar(MultipartFile file, HttpServletRequest request)`
  - 逻辑: 上传 → 删除旧头像 → 更新user_avatar → 返回新URL

- [x] 3.2 新增URL上传端点: `/user/avatar/upload/url`
  - 文件: `src/main/java/com/wuzhenhua/yunpicturebackend/controller/UserController.java`
  - 方法: `uploadAvatarByUrl(UploadAvatarRequest request, HttpServletRequest request)`
  - 逻辑: 调用service处理 → 删除旧头像 → 更新user_avatar → 返回新URL

- [x] 3.3 添加Swagger文档注解
  - 添加 @Operation, @ApiResponses 注解
  - 记录参数和返回值说明

## 4. 依赖注入配置

- [x] 4.1 在 UserController 中注入 AvatarUploadService
  - 注解: `@Resource private AvatarUploadService avatarUploadService;`

- [x] 4.2 在 AvatarUploadServiceImpl 中注入依赖
  - `@Resource private CosManager cosManager;`
  - `@Resource private CosClientConfig cosClientConfig;`

## 5. 编译与验证

- [ ] 5.1 编译项目
  - 命令: `mvn clean compile`
  - 确认: 无编译错误

- [ ] 5.2 启动应用
  - 确认: 无启动错误，端口 8081 正常监听

## 6. 功能测试

- [ ] 6.1 测试文件上传头像
  - 接口: `POST /api/user/avatar/upload`
  - 请求: Form-Data，file 字段上传jpg/png图片
  - 预期: 返回COS URL，user_avatar已更新，返回WebP格式

- [ ] 6.2 测试URL上传头像
  - 接口: `POST /api/user/avatar/upload/url`
  - 请求: `{"fileUrl": "https://example.com/avatar.jpg"}`
  - 预期: 返回COS URL，user_avatar已更新

- [ ] 6.3 测试旧头像删除
  - 操作: 上传新头像，验证旧头像已从COS中删除
  - 预期: 只有新头像存在，旧头像已清理

- [ ] 6.4 测试WebP压缩效果
  - 上传 jpg/png 格式头像
  - 验证COS中存储的是WebP格式
  - 对比大小（预期节省60%-70%）

- [ ] 6.5 测试边界条件
  - 上传超过2MB的文件 → 返回参数错误
  - 上传非图片格式 → 返回参数错误
  - 未登录访问 → 返回401未登录
  - URL格式错误 → 返回参数错误
  - 用户A更新用户B头像 → 返回403无权限

- [ ] 6.6 测试异常处理
  - COS上传失败 → 返回系统错误
  - 旧头像删除失败 → 仅记录日志，新头像保存成功

## 7. API文档更新

- [ ] 7.1 验证Swagger UI文档
  - 访问: `http://localhost:8081/api/swagger-ui.html`
  - 确认: 3个新端点显示正确，参数说明清晰

- [ ] 7.2 更新API使用文档（可选）
  - 说明: 新增3个头像相关端点
  - 示例: 文件上传和URL上传的请求示例

## 8. 性能和存储验证

- [ ] 8.1 验证WebP压缩
  - 上传100个头像
  - 对比WebP vs 原格式的存储大小
  - 确认节省60%-70%

- [ ] 8.2 验证并发更新
  - 同一用户快速上传多个头像
  - 验证key唯一性，无文件覆盖问题

- [ ] 8.3 监控COS配额使用
  - 确认头像存储在 avatar/ 前缀下
  - 监控请求数和流量

## 优先级

**第一阶段（必须）**:
- 1.1, 1.2, 2.1, 3.1, 3.2, 3.3, 4.1, 4.2

**第二阶段（测试）**:
- 6.1-6.6
- 7.1

**第三阶段（监测）**:
- 8.1, 8.2, 8.3
