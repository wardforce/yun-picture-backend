# Gemini 集成设计

## 核心设计理念

### 1. 无状态调用与显式配置 (Explicit Configuration)

**原则**: 不要重用全局 `GenerateContentConfig`。

**说明**:
在 `Gemini.java` 组件中，原本计划在 `@PostConstruct` 阶段初始化一个全局的 `GenerateContentConfig`。

```java
// 这种做法是【垃圾】，因为它限制了灵活性
// config = GenerateContentConfig.builder()
//                 .responseModalities(responseModalities)
//                 .build();
```

相反，我们应该在具体的业务 Service（如 `AiPictureGeneratorServiceImpl`）中根据具体的业务场景（例如需要图片+文本生成）动态构建 `config`。

**原因 (Linus' Taste)**:

1. **消除副作用**: 全局配置会导致不同业务场景之间的隐式耦合。如果一个 Service 需要 `IMAGE`，另一个只需要 `TEXT`，全局配置会迫使所有调用都遵循同一套规则。
2. **显式优于隐式**: 在调用处（Call Site）显式传递 `config` 让代码意图非常清晰。
3. **向后兼容与扩展**: 不同的 Gemini 模型对 `config` 的要求可能不同，将其保持在调用层可以更轻松地针对不同模型进行微调，而无需修改核心 `Gemini` 类。
4. **简洁性**: 既然 `generateContent` 方法本身支持传入 `config`，在 `Gemini` 类里维护一个冗余的字段只会增加复杂性。

### 2. API 身份验证拦截

**设计**: 使用拦截器将 `key` 参数动态添加到 URL 中，而不是依赖 SDK 内部可能存在问题的 Key 管理。
**代码参考**: `Gemini.java:31-52`

## 避坑指南 (Lessons Learned)

- **Config 冗余**: 当 `AiPictureGeneratorServiceImpl` 已经定义并传递了 `config` 时，`Gemini.java` 内部**严禁**再次定义或使用默认 `config`。
- **Modality 约束**: 对于图片生成任务，必须显式在 `config` 中指定 `responseModalities` 为 `["IMAGE", "TEXT"]`。
