# Implementation Tasks

## 1. 数据库 Schema

- [X] 1.1 创建 `chat_history_picture` 关联表
- [X] 1.2 保留 `chat_history.picture_id` 作为主图片ID（向后兼容）

## 2. 实体层

- [X] 2.1 创建 `ChatHistoryPicture` 实体
- [X] 2.2 创建 `ChatHistoryPictureMapper`

## 3. 服务层

- [X] 3.1 创建 `ChatHistoryPictureService` 接口和实现
- [X] 3.2 修改 `ChatHistoryService.saveUserMessage` 支持多图片

## 4. API 模型

- [X] 4.1 修改 `CreateChatRequest.pictureId` 为 `List<Long> pictureIds`
- [X] 4.2 修改 `AiGenerateResponse` 支持返回多张图片

## 5. 业务逻辑

- [X] 5.1 修改 `AiPictureGeneratorServiceImpl.generateAiImage` 支持多图输入
- [X] 5.2 添加输入图片数量校验 (max 14)

## 6. 验证

- [X] 6.1 单元测试：图片关联保存
- [X] 6.2 集成测试：多图生成流程
- [X] 6.3 手动测试：API 调用验证
