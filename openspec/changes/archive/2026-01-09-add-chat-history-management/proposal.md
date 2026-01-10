# Change: 添加聊天历史管理功能

## Why

用户需要管理和查看聊天历史记录，包括按会话（session）查询对话列表、查看会话详情、以及删除对话记录。目前系统只有聊天历史的创建功能，缺乏管理能力。

## What Changes

- **新增能力 `chat-history-management`** - 提供聊天历史的查询和删除管理功能
- **数据库变更** - 为 `chat_history_picture` 表添加 `is_delete` 逻辑删除字段
- **新增 5 个 API 接口**:
  1. 管理员查询所有用户的 session 列表
  2. 用户查询自己的 session 列表
  3. 查询 session 详情（包含关联图片）
  4. 删除整个 session（级联删除）
  5. 删除单条聊天记录

## Impact

- **新增 specs**: `chat-history-management` (新能力)
- **受影响实体**:
  - `ChatHistoryPicture.java` - 添加 `isDelete` 字段
  - `ChatHistoryPictureMapper.xml` - 更新字段映射
- **新增代码**:
  - `ChatHistoryController.java`
  - `ChatHistorySessionQueryRequest.java`
  - `ChatHistoryDetailQueryRequest.java`
  - `DeleteBySessionRequest.java`
  - `ChatHistorySessionVO.java`
  - `ChatHistoryDetailVO.java`
- **修改代码**:
  - `ChatHistoryService.java` - 添加查询和删除方法
  - `ChatHistoryServiceImpl.java` - 实现业务逻辑
  - `ChatHistoryMapper.java` - 添加自定义查询方法
  - `ChatHistoryMapper.xml` - 添加 SQL
