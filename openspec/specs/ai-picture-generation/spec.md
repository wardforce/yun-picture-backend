# Capability: AI 图片生成

## Requirements

### Requirement: 使用 Gemini 引擎生成图片

系统 SHALL 支持通过 Gemini API，结合用户输入的提示词（Prompt）和可选的参考图片，生成新的图片。

#### Scenario: 纯文本生成图片

- **WHEN** 用户仅提供提示词（Prompt）
- **THEN** 系统构建仅包含文本的请求，并配置 `config` 要求生成图片
- **AND** 系统成功返回生成的图片并上传到 COS

#### Scenario: 图片加文本生成图片

- **WHEN** 用户提供提示词和参考图片
- **THEN** 系统先将图片上传并压缩，提取字节流
- **AND** 构建包含文本和图片 Part 的请求，配置 `config` 要求生成图片
- **AND** 系统成功返回生成的图片并上传到 COS

### Requirement: 显式配置管理

所有对 Gemini 的调用 MUST 在调用处显式构建并传递 `GenerateContentConfig`。

#### Scenario: 确保 Config 的生命周期局限于调用

- **WHEN** 业务 Service 调用 Gemini API
- **THEN** 该 Service 负责根据业务需求（如 Response Modalities）构建 `config`
- **AND** 传递给 `Gemini` 组件的 `client` 进行调用
