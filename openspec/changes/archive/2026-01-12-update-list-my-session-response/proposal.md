# Change: 修改用户会话列表接口返回逻辑

## Why

当前 `listMySession` 接口返回结果中会清空 `userId` 字段，用户希望保留该字段以便前端展示。

## What Changes

- **MODIFIED** `listMySession` 接口行为：返回结果保留 `userId` 字段

## Impact

- 受影响 specs: `chat-history-management`
- 受影响代码: `ChatHistoryController.java:79`
