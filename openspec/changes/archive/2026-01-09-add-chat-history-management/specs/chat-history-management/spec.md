## ADDED Requirements

### Requirement: Session 列表查询（管理员）

系统 SHALL 支持管理员查询所有用户的会话（session）列表，显示每个 session 的摘要信息。

#### Scenario: 管理员查询全部 session

- **WHEN** 管理员调用 `/api/chat-history/session/list/admin`
- **THEN** 系统返回所有用户的 session 列表
- **AND** 每个 session 包含：sessionId、firstChatTime、firstPrompt、userId
- **AND** 结果按指定字段排序

#### Scenario: 按时间段过滤

- **WHEN** 管理员提供 startTime 和 endTime 参数
- **THEN** 系统只返回在该时间范围内的 session

#### Scenario: 按 sessionId 精确查询

- **WHEN** 管理员提供 sessionId 参数
- **THEN** 系统只返回匹配的 session

#### Scenario: 非管理员访问被拒绝

- **WHEN** 非管理员用户调用此接口
- **THEN** 系统返回 NO_AUTH_ERROR (40300)

---

### Requirement: Session 列表查询（用户）

系统 SHALL 支持用户查询自己的会话（session）列表。

#### Scenario: 用户查询自己的 session 列表

- **WHEN** 登录用户调用 `/api/chat-history/session/list/my`
- **THEN** 系统只返回属于当前用户的 session 列表
- **AND** 每个 session 包含：sessionId、firstChatTime、firstPrompt
- **AND** 不包含 userId（无需显示）

#### Scenario: 按时间段过滤

- **WHEN** 用户提供 startTime 和 endTime 参数
- **THEN** 系统只返回在该时间范围内的 session

---

### Requirement: Session 详情查询

系统 SHALL 支持查询指定 session 的完整对话历史，包括关联的图片。

#### Scenario: 查询 session 详情

- **WHEN** 用户调用 `/api/chat-history/session/detail` 并提供 sessionId
- **THEN** 系统返回该 session 的所有消息
- **AND** 每条消息包含关联的 `ChatHistoryPicture` 列表

#### Scenario: 权限检查 - 管理员可查看任意 session

- **WHEN** 管理员查询任意 sessionId
- **THEN** 系统返回详情

#### Scenario: 权限检查 - 用户只能查看自己的 session

- **WHEN** 非管理员用户查询不属于自己的 sessionId
- **THEN** 系统返回 NO_AUTH_ERROR (40300)

#### Scenario: 按消息类型过滤

- **WHEN** 用户提供 messageType 参数 (user/ai)
- **THEN** 系统只返回匹配类型的消息

---

### Requirement: Session 级联删除

系统 SHALL 支持删除整个 session，包括所有关联的 ChatHistory 和 ChatHistoryPicture。

#### Scenario: 删除整个 session

- **WHEN** 用户调用 `/api/chat-history/session/delete` 并提供 sessionId
- **AND** 用户是管理员或 session 所有者
- **THEN** 系统逻辑删除该 session 的所有 ChatHistory
- **AND** 系统逻辑删除关联的所有 ChatHistoryPicture
- **AND** 操作在事务内完成

#### Scenario: 权限检查 - 用户只能删除自己的 session

- **WHEN** 非管理员用户尝试删除不属于自己的 sessionId
- **THEN** 系统返回 NO_AUTH_ERROR (40300)

---

### Requirement: 单条记录删除

系统 SHALL 支持删除单条聊天记录，包括关联的 ChatHistoryPicture。

#### Scenario: 删除单条记录

- **WHEN** 用户调用 `/api/chat-history/delete` 并提供 id
- **AND** 用户是管理员或记录所有者
- **THEN** 系统逻辑删除该 ChatHistory 记录
- **AND** 系统逻辑删除关联的 ChatHistoryPicture
- **AND** 操作在事务内完成

#### Scenario: 权限检查 - 用户只能删除自己的记录

- **WHEN** 非管理员用户尝试删除不属于自己的记录
- **THEN** 系统返回 NO_AUTH_ERROR (40300)

---

### Requirement: ChatHistoryPicture 逻辑删除支持

系统 SHALL 支持 `chat_history_picture` 表的逻辑删除。

#### Scenario: 逻辑删除字段

- **WHEN** 系统执行删除操作
- **THEN** 设置 `is_delete = 1` 而非物理删除记录

#### Scenario: 查询自动过滤已删除记录

- **WHEN** 系统查询 ChatHistoryPicture
- **THEN** 自动排除 `is_delete = 1` 的记录

#### Scenario: 数据恢复能力

- **GIVEN** 记录被逻辑删除
- **WHEN** 需要恢复数据
- **THEN** 可通过设置 `is_delete = 0` 恢复
