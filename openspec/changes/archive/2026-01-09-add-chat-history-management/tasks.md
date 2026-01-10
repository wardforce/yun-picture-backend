## 1. 数据库准备

- [x] 1.1 执行 SQL: 为 `chat_history_picture` 添加 `is_delete` 字段
- [x] 1.2 执行 SQL: 为 `chat_history_picture` 添加 `idx_is_delete` 索引
- [x] 1.3 执行 SQL: 为 `chat_history` 添加复合索引 `idx_session_user_time`
- [x] 1.4 执行 SQL: 为 `chat_history` 添加 `idx_message_type` 索引

## 2. 实体类和 Mapper 修改

- [x] 2.1 修改 `ChatHistoryPicture.java`: 添加 `isDelete` 字段和 `@TableLogic` 注解
- [x] 2.2 修改 `ChatHistoryPictureMapper.xml`: 更新 `Base_Column_List` 和 `BaseResultMap`
- [x] 2.3 修改 `ChatHistoryPictureMapper.xml`: 更新 `batchInsert` 方法

## 3. DTO 和 VO 创建

- [x] 3.1 创建 `ChatHistorySessionQueryRequest.java`
- [x] 3.2 创建 `ChatHistoryDetailQueryRequest.java`
- [x] 3.3 创建 `DeleteBySessionRequest.java`
- [x] 3.4 创建 `ChatHistorySessionVO.java`
- [x] 3.5 创建 `ChatHistoryDetailVO.java`

## 4. Mapper 层扩展

- [x] 4.1 修改 `ChatHistoryMapper.java`: 添加 `listSessionSummary` 方法
- [x] 4.2 修改 `ChatHistoryMapper.java`: 添加 `countSessionSummary` 方法
- [x] 4.3 修改 `ChatHistoryMapper.xml`: 实现 `listSessionSummary` SQL

## 5. Service 层实现

- [x] 5.1 修改 `ChatHistoryService.java`: 添加 5 个接口方法
- [x] 5.2 修改 `ChatHistoryServiceImpl.java`: 实现 `listSessionSummary` 方法
- [x] 5.3 修改 `ChatHistoryServiceImpl.java`: 实现 `listSessionDetail` 方法
- [x] 5.4 修改 `ChatHistoryServiceImpl.java`: 实现 `deleteBySessionId` 方法 (事务)
- [x] 5.5 修改 `ChatHistoryServiceImpl.java`: 实现 `deleteByIdWithPermission` 方法 (事务)
- [x] 5.6 修改 `ChatHistoryServiceImpl.java`: 实现 `isSessionOwnedByUser` 方法

## 6. Controller 层创建

- [x] 6.1 创建 `ChatHistoryController.java`
- [x] 6.2 实现 `listSessionByAdmin` API (POST /api/chat-history/session/list/admin)
- [x] 6.3 实现 `listMySession` API (POST /api/chat-history/session/list/my)
- [x] 6.4 实现 `getSessionDetail` API (POST /api/chat-history/session/detail)
- [x] 6.5 实现 `deleteBySession` API (POST /api/chat-history/session/delete)
- [x] 6.6 实现 `deleteById` API (POST /api/chat-history/delete)

## 7. 验证测试

- [x] 7.1 编译验证: `mvn clean compile`
- [x] 7.2 Swagger UI 验证: 确认 5 个 API 接口正确显示
- [x] 7.3 手动测试: 管理员查询 session 列表
- [x] 7.4 手动测试: 用户查询自己的 session 列表
- [x] 7.5 手动测试: 查询 session 详情
- [x] 7.6 手动测试: 删除单条记录
- [x] 7.7 手动测试: 删除整个 session
- [x] 7.8 权限测试: 非管理员无法访问 admin 接口
- [x] 7.9 权限测试: 用户无法操作他人记录
