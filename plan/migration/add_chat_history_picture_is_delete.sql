-- ============================================
-- ChatHistoryPicture 表添加逻辑删除字段
-- 执行时间: 2026-01-06
-- 作者: AI Assistant
-- ============================================

-- 添加 is_delete 字段
ALTER TABLE chat_history_picture
ADD COLUMN is_delete TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除, 1-已删除)';

-- 添加索引优化查询
ALTER TABLE chat_history_picture
ADD INDEX idx_is_delete (is_delete);

-- 验证变更
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    COLUMN_DEFAULT,
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_NAME = 'chat_history_picture'
    AND COLUMN_NAME = 'is_delete';