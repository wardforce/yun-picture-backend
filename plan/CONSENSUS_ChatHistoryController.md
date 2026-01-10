# CONSENSUS - ChatHistoryController 开发共识 (已更新)

## 1. 需求描述

实现聊天历史管理功能,包括:

- 管理员查看所有用户的对话列表
- 用户查看自己的对话列表
- 查看对话详情(包括关联的图片)
- 删除对话(按 session 或单条记录)

---

## 2. 技术决策 (Linus 式实用主义)

### 2.1 关键决策

#### ✅ 决策 1: ChatHistoryPicture 使用逻辑删除

**理由:**

- 保持数据一致性,所有表统一使用逻辑删除
- 数据可恢复,符合生产环境最佳实践
- 避免误删除导致的数据丢失
- 便于后续审计和数据分析

**实现:**

```java
// 先逻辑删除关联的 ChatHistoryPicture (MyBatis-Plus 自动处理)
chatHistoryPictureService.remove(
    new LambdaQueryWrapper<ChatHistoryPicture>()
        .eq(ChatHistoryPicture::getChatHistoryId, chatHistoryId)
);
// 再逻辑删除 ChatHistory
chatHistoryService.removeById(chatHistoryId);
```

**数据库变更:**

```sql
-- 添加逻辑删除字段
ALTER TABLE chat_history_picture 
ADD COLUMN is_delete TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除, 1-已删除)';

-- 添加索引优化查询
ALTER TABLE chat_history_picture 
ADD INDEX idx_is_delete (is_delete);
```

**实体类变更:**

```java
// ChatHistoryPicture.java 需要添加字段
@TableLogic
@Schema(description = "是否删除(0-未删除, 1-已删除)")
private Integer isDelete;
```

#### ✅ 决策 2: "第一条 prompt" 定义

**理解:**

- `messageType = 'user'` 的消息即为用户的 prompt
- 取 `MIN(createTime)` 的 user 消息作为"第一条 prompt"

**SQL 逻辑:**

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

#### ✅ 决策 3: 查询详情只返回基础字段

**理由:**

- 避免过度设计,只返回必要信息
- 如果需要完整 Picture 对象,前端可以再次请求

**返回字段:**

- `ChatHistory` 的所有字段
- `ChatHistoryPicture` 的所有字段 (不包括关联的 Picture 实体)

#### ✅ 决策 4: 排序字段限制

**支持的排序字段:**

- `createTime` (默认)
- `userId`
- `sessionId`

**不支持:**

- `message` 内容排序 (无实际业务意义)

---

## 3. API 接口设计

### 3.1 接口列表

| 方法   | 路径                                     | 权限        | 说明                        |
| ------ | ---------------------------------------- | ----------- | --------------------------- |
| GET    | `/api/chat-history/session/list/admin` | Admin       | 管理员查询所有 session 列表 |
| GET    | `/api/chat-history/session/list/my`    | User        | 用户查询自己的 session 列表 |
| GET    | `/api/chat-history/session/detail`     | Admin/Owner | 查询 session 详情           |
| DELETE | `/api/chat-history/session/delete`     | Admin/Owner | 删除整个 session            |
| DELETE | `/api/chat-history/delete`             | Admin/Owner | 删除单条记录                |

### 3.2 请求/响应对象

#### ChatHistorySessionQueryRequest (需求 1/2)

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatHistorySessionQueryRequest extends PageRequest {
    private Long sessionId;        // 可选,精确查询
    private Long userId;           // 可选,管理员用
    private Date startTime;        // 时间段开始
    private Date endTime;          // 时间段结束
}
```

#### ChatHistorySessionVO (需求 1/2 返回)

```java
@Data
public class ChatHistorySessionVO {
    private Long sessionId;
    private Date firstChatTime;
    private String firstPrompt;
    private Long userId;           // 仅管理员接口返回
}
```

#### ChatHistoryDetailQueryRequest (需求 3)

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class ChatHistoryDetailQueryRequest extends PageRequest {
    @NotNull
    private Long sessionId;        // 必填
    private String messageType;    // 可选: user/ai
    private Date startTime;
    private Date endTime;
}
```

#### ChatHistoryDetailVO (需求 3 返回)

```java
@Data
public class ChatHistoryDetailVO {
    private Long id;
    private String message;
    private String messageType;
    private Long pictureId;
    private Long userId;
    private Long sessionId;
    private Date createTime;
    private List<ChatHistoryPicture> pictures;  // 关联的图片列表
}
```

#### DeleteBySessionRequest 

```java
@Data
public class DeleteBySessionRequest {
    @NotNull
    private Long sessionId;
}
```

需求 4,这个就不需要用request来包了，就一个字段不用包

---

## 4. 数据库查询策略

### 4.1 需求 1/2: Session 列表查询

**挑战:** 需要 GROUP BY + 子查询获取第一条 prompt

**方案:** 使用 MyBatis-Plus + 自定义 SQL

```xml
<select id="listSessionSummary" resultType="ChatHistorySessionVO">
    SELECT 
        ch.session_id as sessionId,
        MIN(ch.create_time) as firstChatTime,
        ch.user_id as userId,
        (SELECT message 
         FROM chat_history 
         WHERE session_id = ch.session_id 
           AND message_type = 'user' 
           AND is_delete = 0
         ORDER BY create_time ASC 
         LIMIT 1) as firstPrompt
    FROM chat_history ch
    WHERE ch.is_delete = 0
      <if test="userId != null">
          AND ch.user_id = #{userId}
      </if>
      <if test="sessionId != null">
          AND ch.session_id = #{sessionId}
      </if>
      <if test="startTime != null">
          AND ch.create_time >= #{startTime}
      </if>
      <if test="endTime != null">
          AND ch.create_time <= #{endTime}
      </if>
    GROUP BY ch.session_id, ch.user_id
    ORDER BY ${sortField} ${sortOrder}
    LIMIT #{offset}, #{pageSize}
</select>
```

### 4.2 需求 3: Session 详情查询

**方案:** 分两步查询

1. 查询 `ChatHistory` 列表
2. 批量查询关联的 `ChatHistoryPicture`

```java
// Step 1: 查询 ChatHistory
List<ChatHistory> historyList = chatHistoryService.list(
    new LambdaQueryWrapper<ChatHistory>()
        .eq(ChatHistory::getSessionId, sessionId)
        .eq(ChatHistory::getIsDelete, 0)
        // 其他过滤条件...
        .orderBy(true, isAsc, orderByField)
);

// Step 2: 批量查询 ChatHistoryPicture (MyBatis-Plus 自动添加 is_delete = 0)
List<Long> historyIds = historyList.stream()
    .map(ChatHistory::getId)
    .collect(Collectors.toList());
  
List<ChatHistoryPicture> pictures = chatHistoryPictureService.list(
    new LambdaQueryWrapper<ChatHistoryPicture>()
        .in(ChatHistoryPicture::getChatHistoryId, historyIds)
);

// Step 3: 组装 VO
// ...
```

### 4.3 需求 4/5: 级联删除

**方案:** 事务保证原子性

```java
@Transactional(rollbackFor = Exception.class)
public void deleteBySessionId(Long sessionId) {
    // 1. 查询该 session 的所有 ChatHistory ID
    List<Long> historyIds = chatHistoryService.list(
        new LambdaQueryWrapper<ChatHistory>()
            .eq(ChatHistory::getSessionId, sessionId)
            .select(ChatHistory::getId)
    ).stream().map(ChatHistory::getId).collect(Collectors.toList());
  
    // 2. 逻辑删除 ChatHistoryPicture (MyBatis-Plus 自动处理)
    chatHistoryPictureService.remove(
        new LambdaQueryWrapper<ChatHistoryPicture>()
            .in(ChatHistoryPicture::getChatHistoryId, historyIds)
    );
  
    // 3. 逻辑删除 ChatHistory
    chatHistoryService.removeByIds(historyIds);
}
```

---

## 5. 权限控制策略

### 5.1 权限矩阵

| 接口               | 管理员 | 记录所有者    | 其他用户 |
| ------------------ | ------ | ------------- | -------- |
| session/list/admin | ✅     | ❌            | ❌       |
| session/list/my    | ✅     | ✅            | ❌       |
| session/detail     | ✅     | ✅ (仅自己的) | ❌       |
| session/delete     | ✅     | ✅ (仅自己的) | ❌       |
| delete             | ✅     | ✅ (仅自己的) | ❌       |

### 5.2 权限检查代码模板

```java
// 检查是否为管理员或记录所有者
private void checkPermission(Long targetUserId, HttpServletRequest request) {
    User loginUser = userService.getLoginUser(request);
    if (!userService.isAdmin(loginUser) && 
        !loginUser.getId().equals(targetUserId)) {
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
    }
}
```

---

## 6. 验收标准

### 6.1 功能验收

- [ ] 管理员可以查看所有用户的 session 列表
- [ ] 用户只能查看自己的 session 列表
- [ ] session 列表正确显示第一条 prompt 和最早时间
- [ ] 时间段过滤、session_id 过滤正常工作
- [ ] 排序功能正常(升序/降序)
- [ ] session 详情正确显示所有消息和关联图片
- [ ] 删除 session 时正确级联删除所有关联数据
- [ ] 删除单条记录时正确删除关联图片
- [ ] 权限控制正确:非管理员无法查看/删除他人数据
- [ ] **逻辑删除正确:数据标记为已删除但仍存在于数据库**

### 6.2 性能验收

- [ ] session 列表查询响应时间 < 500ms (1000 条记录)
- [ ] session 详情查询响应时间 < 300ms (100 条消息)
- [ ] 删除操作响应时间 < 200ms

### 6.3 代码质量

- [ ] 所有方法都有 Swagger 注解
- [ ] 所有参数都有校验
- [ ] 所有异常都有合适的错误码
- [ ] 代码复用率高,无重复逻辑
- [ ] 事务边界清晰

---

## 7. 技术约束

### 7.1 必须遵守

- ✅ 使用 MyBatis-Plus 的逻辑删除 (`@TableLogic`)
- ✅ 继承 `PageRequest` 实现分页
- ✅ 使用 `@Transactional` 保证删除操作的原子性
- ✅ 遵循现有的命名规范和注解规范
- ✅ 使用 `BaseResponse<T>` 包装返回结果

### 7.2 禁止事项

- ❌ 不修改 `ChatHistory` 实体的现有字段
- ❌ 不修改现有的聊天生成逻辑
- ❌ 不破坏现有的 API 接口

---

## 8. 集成方案

### 8.1 新增文件清单

**Controller:**

- `ChatHistoryController.java`

**DTO:**

- `dto/chathistory/ChatHistorySessionQueryRequest.java`
- `dto/chathistory/ChatHistoryDetailQueryRequest.java`
- `dto/chathistory/DeleteBySessionRequest.java`

**VO:**

- `vo/ChatHistorySessionVO.java`
- `vo/ChatHistoryDetailVO.java`

**Migration:**

- `migration/add_chat_history_picture_is_delete.sql`

**Mapper:**

- 修改 `ChatHistoryMapper.java` (添加自定义查询方法)
- 修改 `ChatHistoryMapper.xml` (添加 SQL)

**Service:**

- 修改 `ChatHistoryService.java` (添加业务方法)
- 修改 `ChatHistoryServiceImpl.java` (实现业务逻辑)

**Entity:**

- 修改 `ChatHistoryPicture.java` (添加 `isDelete` 字段)

### 8.2 修改文件清单

**实体类:**

- `model/entity/ChatHistoryPicture.java` (添加 `isDelete` 字段)

**Mapper XML:**

- `mapper/ChatHistoryPictureMapper.xml` (更新字段列表)

### 8.3 依赖关系

```
ChatHistoryController
  ├── ChatHistoryService (新增方法)
  ├── ChatHistoryPictureService (已有)
  └── UserService (权限检查)
```

---

## 9. 风险评估

### 9.1 技术风险

⚠️ **中等风险: GROUP BY 查询性能**

- **问题**: session 列表查询使用 GROUP BY + 子查询,数据量大时可能慢
- **缓解**:
  - 在 `session_id` 和 `create_time` 上建立复合索引
  - 限制时间范围查询
  - 考虑后续引入 Redis 缓存

⚠️ **低风险: 级联删除事务**

- **问题**: 删除大量数据时可能超时
- **缓解**:
  - 限制单次删除的记录数
  - 使用批量删除优化

⚠️ **低风险: 数据库迁移**

- **问题**: 添加字段可能影响现有功能
- **缓解**:
  - 先在测试环境验证
  - 准备回滚脚本
  - 执行前备份数据库

### 9.2 业务风险

✅ **无破坏性风险**

- 新增功能,不影响现有业务
- 使用逻辑删除,数据可恢复
- 数据库变更向后兼容

---

## 10. 下一步行动

已完成 **Architect** 阶段,下一步:

1. ✅ 执行数据库迁移 (添加 `is_delete` 字段)
2. ✅ 更新实体类和 Mapper XML
3. 进入 **Atomize** 阶段,按任务拆分执行
