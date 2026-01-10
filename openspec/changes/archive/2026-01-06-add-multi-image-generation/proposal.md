# Change: Add Multi-Image Generation Support

## Why

当前 AI 图片生成功能仅支持单张输入图片和单张输出图片。用户需要使用最多 **14 张参考图片** 来生成新图片，并且需要在对话历史中 **追踪每张图片的 pictureId**。

## What Changes

### 数据库层
- **新增表 `chat_history_picture`** - 存储对话消息与图片的多对多关系

### 实体层
- **新增 `ChatHistoryPicture`** - 关联实体
- **修改 `CreateChatRequest`** - `pictureId` 从 `Long` 改为 `List<Long>` (支持 1-14 张)

### 服务层
- **修改 `AiPictureGeneratorServiceImpl.generateAiImage`** - 支持多图片输入
- **新增 `ChatHistoryPictureService`** - 管理图片关联

### 响应层
- **修改 `AiGenerateResponse`** - 返回多张生成图片的 pictureId 列表

## Impact

- **Affected specs**: `ai-picture-generation`
- **Affected code**:
  - `model/entity/ChatHistory.java` - 移除 `pictureId` 字段 (**BREAKING**)
  - `model/entity/ChatHistoryPicture.java` - 新增
  - `mapper/ChatHistoryPictureMapper.java` - 新增
  - `service/ChatHistoryPictureService.java` - 新增
  - `api/gemini/model/CreateChatRequest.java` - 修改
  - `api/gemini/model/AiGenerateResponse.java` - 修改
  - `api/gemini/service/impl/AiPictureGeneratorServiceImpl.java` - 修改
  - `sql/create_table.sql` - 新增表结构
