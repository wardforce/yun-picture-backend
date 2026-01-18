# chat-history-management Specification

## Purpose
TBD - created by archiving change update-list-my-session-response. Update Purpose after archive.
## Requirements
### Requirement: User Session List Query

用户 SHALL 能够查询自己的会话列表，返回结果 SHALL 包含以下字段：
- sessionId - 会话ID
- firstChatTime - 首次对话时间
- firstPrompt - 首条用户消息
- userId - 用户ID
- spaceId - 空间ID（如有）

系统 SHALL 过滤 `is_delete = 1` 的已删除记录。

#### Scenario: User queries own sessions with userId returned
- **WHEN** 用户调用 `POST /api/chat-history/session/list/my`
- **THEN** 返回用户自己的会话列表
- **AND** 每条记录包含 `userId` 字段
- **AND** 不包含 `is_delete = 1` 的记录

