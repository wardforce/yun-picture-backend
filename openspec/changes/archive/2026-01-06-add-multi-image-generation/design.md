# Design: Multi-Image Generation

## Context

Gemini API 支持最多 **14 张参考图片** 用于图片生成。当前系统设计仅支持单张图片输入和输出，需要扩展以支持多图场景。

## Goals / Non-Goals

**Goals:**

- 支持用户上传最多 14 张参考图片
- 每张输入/输出图片都记录对应的 `pictureId`
- 保持 API 向后兼容（单图场景仍然可用）

**Non-Goals:**

- 不改变 Gemini API 调用方式（仅修改输入构建）
- 不修改图片上传逻辑

## Decisions

### 方案选择：新增关联表 vs 修改现有字段

**选择：新增 `chat_history_picture` 关联表**

```
┌─────────────────┐       ┌──────────────────────┐       ┌─────────┐
│  chat_history   │ 1───N │ chat_history_picture │ N───1 │ picture │
└─────────────────┘       └──────────────────────┘       └─────────┘
```

**理由:**

1. **灵活性** - 支持任意数量的图片关联
2. **清晰的关系** - 可区分输入图片 (INPUT) 和输出图片 (OUTPUT)
3. **可扩展** - 未来可添加排序、权重等属性

### 关联表结构

```sql
CREATE TABLE chat_history_picture (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_history_id BIGINT NOT NULL COMMENT '对话历史ID，与chat_history的session_id一致',
    picture_id      BIGINT NOT NULL COMMENT '图片ID',
    picture_type    VARCHAR(16) NOT NULL COMMENT '图片类型: INPUT/OUTPUT',
    sort_order      INT DEFAULT 0 COMMENT '排序顺序',
    create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
  
    INDEX idx_chat_history_id (chat_history_id),
    INDEX idx_picture_id (picture_id)
) COMMENT '对话历史图片关联表';
```

### API 变更

**Request:**

```java
// Before
Long pictureId;

// After
List<Long> pictureIds;  // 支持 1-14 张，null/empty 表示纯文本生成
```

**Response:**

```java
// Before
PictureVO pictureVO;

// After
List<PictureVO> pictureVOs;  // 生成的图片列表
```

## Risks / Trade-offs

| Risk       | Mitigation                                                |
| ---------- | --------------------------------------------------------- |
| 数据迁移   | 保留 `chat_history.picture_id` 字段作为主图片的冗余存储 |
| API 兼容性 | 前端需同步更新，先协调发布计划                            |
| 性能       | 关联查询增加，添加合适索引                                |

## Migration Plan

1. 创建新表 `chat_history_picture`
2. 迁移现有数据：将 `chat_history.picture_id` 同步到关联表
3. 更新 Service 层逻辑
4. 保留 `picture_id` 字段一段时间后再考虑删除

## Open Questions

1. **前端发布协调** - 需要同步更新前端 API 调用
2. **历史数据迁移** - 是否需要迁移脚本？
