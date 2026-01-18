## 1. Implementation

- [x] 1.1 修改 `ChatHistoryController.java:79`: `listSessionSummary(request, false)` → `listSessionSummary(request, true)`

## 2. Verification

- [x] 2.1 编译验证: `mvn clean compile`
- [x] 2.2 接口测试: POST `/api/chat-history/session/list/my` 返回 userId 字段
- [x] 2.3 确认 is_delete=1 的记录不显示
