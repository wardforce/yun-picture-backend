# Change: 添加用户头像上传功能

## Why

当前用户无法更新自己的头像，头像字段在User表中存在但无法通过API更新。用户需要一个便捷的头像上传功能，支持文件上传和URL上传，上传的头像应进行WebP压缩以节省COS存储空间。

需要增加：
1. **头像文件上传** - 支持jpg/png等格式上传头像
2. **头像URL上传** - 支持通过URL导入头像
3. **WebP压缩** - 自动压缩为WebP格式节省存储空间
4. **旧头像清理** - 更新头像时自动删除COS中的旧文件

## What Changes

- **ADDED** 用户头像上传功能（文件上传和URL上传）
- **ADDED** `AvatarUploadService` 服务层（处理头像上传和删除）
- **ADDED** WebP压缩处理（使用COS的图片处理能力）
- **ADDED** 旧头像自动清理机制
- **ADDED** 新API端点：
  - `POST /user/avatar/upload` - 文件上传头像
  - `POST /user/avatar/upload/url` - URL上传头像
- **ADDED** `UploadAvatarRequest` DTO - URL上传请求

**头像规格**:
- 大小限制：2MB（独立于图片库的10MB限制）
- 格式限制：jpg, jpeg, png, webp
- 存储格式：自动转换为WebP以节省空间（可选原图备份）
- URL命名规则：`avatar/{userId}_{timestamp}.webp`
- 旧头像：更新时自动删除COS中的旧文件

## Impact

- **受影响 specs**: `user-profile`, `file-upload`（新增 capability）
- **受影响代码**:
  - `src/main/java/com/wuzhenhua/yunpicturebackend/service/AvatarUploadService.java` - 新增
  - `src/main/java/com/wuzhenhua/yunpicturebackend/service/impl/AvatarUploadServiceImpl.java` - 新增
  - `src/main/java/com/wuzhenhua/yunpicturebackend/controller/UserController.java` - 修改/新增 3 个方法
  - `src/main/java/com/wuzhenhua/yunpicturebackend/model/dto/user/UploadAvatarRequest.java` - 新增

- **COS存储变更**:
  - 头像统一存储在 `avatar/` 前缀下
  - 自动WebP压缩，节省存储空间（预估节省60%-70%）
  - 旧头像删除，不产生垃圾文件

- **数据库变更**: 无（user.user_avatar 字段已存在）
- **配置变更**: 无（使用现有COS配置）
- **向后兼容性**: ✅ 兼容 - 不影响现有功能，user_avatar 字段可为空

## Dependencies

- 腾讯云COS配置（已配置）
- 依赖 `CosManager` 和 `CosClientConfig`（已存在）
- Spring Boot Web依赖（已安装）

## Performance Considerations

- **上传速度**: ~250ms (单个2MB文件)
- **WebP压缩**: 由COS服务器处理，不影响应用性能
- **存储节省**: WebP格式相比原格式节省60%-70%
- **并发能力**: 使用 timestamp+userId 保证key唯一性，支持并发更新

## Security Considerations

- 用户只能更新自己的头像（通过 `getLoginUser()` 验证）
- 文件类型和大小限制
- MIME类型检验
- 删除旧头像时的异常容错（不阻塞主流程）

## Testing Strategy

- 单元测试：文件上传、URL上传、验证逻辑
- 集成测试：包含旧头像删除验证
- COS验证：确认压缩效果和文件清理
- 边界测试：超大文件、错误格式、并发更新
