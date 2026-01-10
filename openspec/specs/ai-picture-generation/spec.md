# Capability: AI 图片生成

## Purpose

提供基于 Gemini API 的 AI 图片生成能力，支持纯文本或结合参考图片生成新图片。
## Requirements
### Requirement: 使用 Gemini 引擎生成图片

系统 SHALL 支持通过 Gemini API，结合用户输入的提示词（Prompt）和可选的多张参考图片（最多14张），生成新的图片。

#### Scenario: 纯文本生成图片
- **WHEN** 用户仅提供提示词（Prompt）
- **THEN** 系统构建仅包含文本的请求，并配置 `config` 要求生成图片
- **AND** 系统成功返回生成的图片并上传到 COS

#### Scenario: 单图+文本生成图片

- **WHEN** 用户提供提示词和1张参考图片
- **THEN** 系统先将图片上传并压缩，提取字节流
- **AND** 构建包含文本和图片 Part 的请求，配置 `config` 要求生成图片
- **AND** 系统成功返回生成的图片并上传到 COS

#### Scenario: 多图+文本生成图片

- **WHEN** 用户提供提示词和2-14张参考图片
- **THEN** 系统获取所有图片的缩略图
- **AND** 构建包含文本和所有图片 Part 的请求
- **AND** 系统成功返回生成的图片列表并上传到 COS

### Requirement: 显式配置管理

所有对 Gemini 的调用 MUST 在调用处显式构建并传递 `GenerateContentConfig`。

#### Scenario: 确保 Config 的生命周期局限于调用

- **WHEN** 业务 Service 调用 Gemini API
- **THEN** 该 Service 负责根据业务需求（如 Response Modalities）构建 `config`
- **AND** 传递给 `Gemini` 组件的 `client` 进行调用

### Requirement: Multi-Image Input Support

系统 SHALL 支持用户提供最多 14 张参考图片用于 AI 图片生成。

#### Scenario: 多图输入生成

- **WHEN** 用户提供 N 张图片 (1 ≤ N ≤ 14) 和 prompt
- **THEN** 系统将所有图片发送给 Gemini API
- **AND** 系统记录每张输入图片的 pictureId 到对话历史

#### Scenario: 超过图片数量限制

- **WHEN** 用户提供超过 14 张图片
- **THEN** 系统返回参数错误 (PARAMS_ERROR)
- **AND** 错误信息提示最多支持 14 张图片

#### Scenario: 向后兼容单图输入

- **WHEN** 用户仅提供 1 张图片
- **THEN** 系统行为与之前单图模式一致

---

### Requirement: Picture Tracking in Chat History

系统 MUST 在对话历史中追踪所有关联图片的 pictureId。

#### Scenario: 用户消息关联输入图片

- **WHEN** 用户发送包含图片的消息
- **THEN** 所有输入图片的 pictureId 记录到 `chat_history_picture` 表
- **AND** `picture_type` 设置为 `INPUT`

#### Scenario: AI 消息关联输出图片

- **WHEN** AI 生成图片响应
- **THEN** 所有生成图片的 pictureId 记录到 `chat_history_picture` 表
- **AND** `picture_type` 设置为 `OUTPUT`

---

