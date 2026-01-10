# ALIGNMENT - ChatHistoryController 开发需求对齐

## 1. 项目上下文分析

### 1.1 现有数据结构

**核心实体关系:**
```
ChatHistory (chat_history)
├── id: Long (主键)
├── message: String (消息内容)
├── messageType: String (user/ai)
├── pictureId: Long (原图ID)
├── userId: Long (创建用户ID)
├── sessionId: Long (对话会话ID) ⭐ 核心字段
├── createTime: Date
├── updateTime: Date
└── isDelete: Integer (逻辑删除)

ChatHistoryPicture (chat_history_picture)
├── id: Long (主键)
├── chatHistoryId: Long (外键 → ChatHistory.id)
├── pictureId: Long (图片ID)
├── pictureType: String (INPUT/OUTPUT)
├── sortOrder: Integer (排序)
└── createTime: Date
```

**关键关系:**
- `ChatHistory.sessionId` 用于标识同一轮对话
- `ChatHistoryPicture.chatHistoryId` 关联到 `ChatHistory.id`
- 一个 `ChatHistory` 可以有多个 `ChatHistoryPicture`

### 1.2 现有技术栈
- **框架**: Spring Boot + MyBatis-Plus
- **数据库**: MySQL
- **权限控制**: 基于 `UserService` 的 admin 判断
- **分页**: 继承 `PageRequest` (包含 current, pageSize, sortField, sortOrder)
- **文档**: Swagger/OpenAPI 注解

### 1.3 现有代码模式
参考 `PictureController`:
- 使用 `@RestController` + `@RequestMapping`
- 请求对象继承 `PageRequest`
- 返回 `BaseResponse<T>` 包装结果
- 权限检查: `userService.isAdmin(request)` 或 `loginUser.getId().equals(targetUserId)`
- 逻辑删除: MyBatis-Plus 的 `@TableLogic`

---

## 2. 需求理解与规范化

### 2.1 原始需求
来自 `plan/chatHistroyController文档.md`:

1. **查询 session_id 列表 (Admin Only)**
   - 显示每个 session_id 最早的聊天记录
   - 字段: 最早创建时间、第一条 prompt、所属用户 ID
   - 过滤: 时间段、session_id
   - 排序: 可选字段 + 升序/降序

2. **查询 session_id 列表 (User Specific)**
   - 显示当前用户的 session_id 列表
   - 字段: 最早创建时间、第一条 prompt
   - 过滤: 时间段

3. **查询 session_id 详情**
   - 显示同一 session_id 下的全部 ChatHistory 和关联的 ChatHistoryPicture
   - 权限: 管理员 或 聊天用户本人
   - 过滤: 除 session_id 和 user_id 外的全部字段
   - 排序: 可选字段

4. **删除 session_id 全部记录**
   - 删除该 session_id 的所有 ChatHistory 和关联的 ChatHistoryPicture
   - 权限: 管理员 或 当前用户 ID == session 的 user_id

5. **删除单条聊天记录**
   - 删除指定 id 的 ChatHistory 和关联的 ChatHistoryPicture
   - 权限: 管理员 或 当前用户 ID == 记录的 user_id

---

## 3. Linus 式分析

### 3.1 数据结构审查
✅ **好的设计:**
- `sessionId` 字段已存在,无需修改表结构
- `ChatHistoryPicture.chatHistoryId` 外键关系清晰

⚠️ **潜在问题:**
- 需求 1 和 2 要求"第一条 prompt",但 `ChatHistory` 没有明确区分 prompt 和 response
- 需要通过 `messageType = 'user'` 来识别用户的 prompt

### 3.2 复杂度分析
🟡 **中等复杂度:**
- 需求 1/2: 需要 `GROUP BY sessionId` + `MIN(createTime)` 的 SQL
- 需求 3: 需要关联查询 `ChatHistory` 和 `ChatHistoryPicture`
- 需求 4/5: 级联删除需要先删除 `ChatHistoryPicture`,再删除 `ChatHistory`

### 3.3 特殊情况识别
❌ **需要消除的特殊情况:**
- 删除操作的权限检查逻辑重复 → 抽取为通用方法
- session_id 列表查询的 SQL 逻辑相似 → 复用查询逻辑

### 3.4 破坏性分析
✅ **零破坏性:**
- 新增 Controller,不修改现有代码
- 使用逻辑删除,不影响现有数据

---

## 4. 边界确认

### 4.1 明确的边界
✅ **在范围内:**
- 5 个 API 方法的实现
- 权限控制逻辑
- 分页、排序、过滤功能

❌ **不在范围内:**
- 修改现有 `ChatHistory` 或 `ChatHistoryPicture` 实体
- 修改现有的聊天生成逻辑
- 前端页面开发

### 4.2 技术约束
- 必须使用 MyBatis-Plus 的逻辑删除
- 必须继承现有的 `PageRequest`
- 必须遵循现有的 Controller 命名和注解规范

---

## 5. 疑问澄清

### 5.1 已解决的疑问
✅ **Q1: "第一条 prompt" 如何定义?**
- **A**: 通过 `messageType = 'user'` 且 `MIN(createTime)` 来获取

✅ **Q2: 删除是物理删除还是逻辑删除?**
- **A**: 使用 MyBatis-Plus 的逻辑删除 (`isDelete = 1`)

✅ **Q3: session_id 列表是否需要分页?**
- **A**: 是,继承 `PageRequest`

### 5.2 需要确认的问题
⚠️ **Q1: ChatHistoryPicture 是否也需要逻辑删除?**
- **当前状态**: `ChatHistoryPicture` 实体没有 `isDelete` 字段
- **建议**: 
  - 方案 A: 添加 `isDelete` 字段,使用逻辑删除 (推荐)
  - 方案 B: 物理删除 `ChatHistoryPicture`
- **影响**: 如果选择方案 A,需要修改实体和表结构

⚠️ **Q2: 需求 3 的"除 session_id 和 user_id 外的全部字段"是否包括关联的 Picture 信息?**
- **理解**: 查询条件可以包括 `message`, `messageType`, `createTime` 等
- **需要确认**: 是否需要返回完整的 `Picture` 对象,还是只返回 `pictureId`?

⚠️ **Q3: 需求 1 的排序字段是否包括 "第一条 prompt" 的内容?**
- **理解**: 可以按 `createTime`, `userId` 等排序
- **需要确认**: 是否需要按 `message` (第一条 prompt 内容) 排序?

---

## 6. 下一步行动

1. **等待用户确认** 上述 3 个问题
2. 确认后生成 **CONSENSUS** 文档
3. 进入 **Architect** 阶段,设计 API 接口和 SQL 查询

---

## 附录: 参考资料

### A.1 现有 Controller 方法示例
参考 `PictureController.listPictureVOByPage`:
```java
@PostMapping("/list/page/vo")
public BaseResponse<Page<PictureVO>> listPictureVOByPage(
    @RequestBody PictureQueryRequest pictureQueryRequest,
    HttpServletRequest request) {
    // 1. 参数校验
    // 2. 权限检查
    // 3. 调用 Service 查询
    // 4. 返回结果
}
```

### A.2 权限检查示例
```java
// 管理员检查
if (!userService.isAdmin(request)) {
    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
}

// 用户本人检查
User loginUser = userService.getLoginUser(request);
if (!loginUser.getId().equals(targetUserId) && !userService.isAdmin(request)) {
    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
}
```
