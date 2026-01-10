## Context

聊天历史管理是 AI 图片生成功能的配套能力。用户在使用 AI 图片生成后，需要能够回顾历史对话、管理会话记录。

**现有数据模型:**
- `chat_history` 表：存储对话消息，已有 `session_id` 字段标识会话，使用 `is_delete` 逻辑删除
- `chat_history_picture` 表：存储对话与图片的关联关系，**缺少** `is_delete` 字段

**约束:**
- 权限控制：管理员可查看/删除所有记录，用户只能操作自己的记录
- 数据一致性：所有表统一使用逻辑删除
- 性能：session 列表查询需要 GROUP BY + 子查询，需考虑索引优化

## Goals / Non-Goals

**Goals:**
- 提供 session 列表查询（分页、过滤、排序）
- 提供 session 详情查询（包含关联图片）
- 提供级联删除功能（session 或单条记录）
- 统一使用逻辑删除

**Non-Goals:**
- 不修改现有的聊天生成逻辑
- 不提供聊天记录的编辑功能
- 不提供批量删除功能（单次只删除一个 session 或一条记录）

## Decisions

### Decision 1: ChatHistoryPicture 使用逻辑删除

**决策:** 为 `chat_history_picture` 表添加 `is_delete` 字段

**理由:**
- 保持数据一致性，所有表统一使用逻辑删除
- 数据可恢复，符合生产环境最佳实践
- MyBatis-Plus 自动处理逻辑删除条件

**替代方案:**
- 物理删除：更简单，但数据不可恢复
- 不修改：无法实现级联逻辑删除

### Decision 2: Session 列表使用子查询获取第一条 prompt

**决策:** 使用 SQL 子查询获取每个 session 的第一条用户消息

```sql
SELECT 
    session_id,
    MIN(create_time) as first_chat_time,
    (SELECT message FROM chat_history 
     WHERE session_id = ch.session_id 
       AND message_type = 'user' 
       AND is_delete = 0
     ORDER BY create_time ASC 
     LIMIT 1) as first_prompt,
    user_id
FROM chat_history ch
WHERE is_delete = 0
GROUP BY session_id, user_id
```

**理由:**
- 符合需求：显示每个 session 的第一条 prompt
- 性能可接受：通过索引优化

### Decision 3: 批量查询避免 N+1 问题

**决策:** session 详情查询分两步执行

1. 查询 `ChatHistory` 列表
2. 批量查询关联的 `ChatHistoryPicture`
3. 在内存中组装 VO

**理由:**
- 避免 N+1 查询问题
- 代码逻辑清晰

## Risks / Trade-offs

| 风险 | 等级 | 缓解措施 |
|------|------|---------|
| GROUP BY 查询性能 | 中 | 建立复合索引 `idx_session_user_time` |
| 数据库迁移失败 | 中 | 备份数据库，准备回滚脚本 |
| 级联删除事务超时 | 低 | 限制单次删除记录数 |

## Migration Plan

1. **备份数据库**
2. **执行 SQL 迁移:**
   ```sql
   ALTER TABLE chat_history_picture 
   ADD COLUMN is_delete TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除, 1-已删除)';
   
   ALTER TABLE chat_history_picture 
   ADD INDEX idx_is_delete (is_delete);
   ```
3. **更新实体类和 Mapper XML**
4. **部署新代码**
5. **验证功能正常**

**回滚方案:**
```sql
ALTER TABLE chat_history_picture DROP INDEX idx_is_delete;
ALTER TABLE chat_history_picture DROP COLUMN is_delete;
```

## Open Questions

无 - 所有技术问题已在之前的规划中解决
