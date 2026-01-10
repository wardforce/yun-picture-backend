# 数据库变更 - ChatHistoryPicture 添加逻辑删除

## 变更说明

为 `chat_history_picture` 表添加逻辑删除字段 `is_delete`,保持与 `chat_history` 表的一致性。

---

## SQL 脚本

### 1. 添加逻辑删除字段

```sql
-- 添加 is_delete 字段
ALTER TABLE chat_history_picture 
ADD COLUMN is_delete TINYINT DEFAULT 0 COMMENT '是否删除(0-未删除, 1-已删除)';

-- 添加索引优化查询
ALTER TABLE chat_history_picture 
ADD INDEX idx_is_delete (is_delete);
```

### 2. 验证变更

```sql
-- 查看表结构
DESC chat_history_picture;

-- 查看索引
SHOW INDEX FROM chat_history_picture;
```

---

## 实体类变更

### ChatHistoryPicture.java

需要在 `ChatHistoryPicture` 实体类中添加以下字段:

```java
/**
 * 是否删除
 */
@TableLogic
@Schema(description = "是否删除(0-未删除, 1-已删除)")
private Integer isDelete;
```

**完整的实体类应该包含:**
- id
- chatHistoryId
- pictureId
- pictureType
- sortOrder
- createTime
- **isDelete** ⭐ 新增字段

---

## Mapper XML 变更

### ChatHistoryPictureMapper.xml

需要更新 `Base_Column_List`:

```xml
<sql id="Base_Column_List">
    id, chat_history_id, picture_id, picture_type, sort_order, create_time, is_delete
</sql>
```

需要更新 `BaseResultMap`:

```xml
<resultMap id="BaseResultMap" type="com.wuzhenhua.yunpicturebackend.model.entity.ChatHistoryPicture">
    <id property="id" column="id" jdbcType="BIGINT"/>
    <result property="chatHistoryId" column="chat_history_id" jdbcType="BIGINT"/>
    <result property="pictureId" column="picture_id" jdbcType="BIGINT"/>
    <result property="pictureType" column="picture_type" jdbcType="VARCHAR"/>
    <result property="sortOrder" column="sort_order" jdbcType="INTEGER"/>
    <result property="createTime" column="create_time" jdbcType="TIMESTAMP"/>
    <result property="isDelete" column="is_delete" jdbcType="TINYINT"/>
</resultMap>
```

需要更新 `batchInsert`:

```xml
<insert id="batchInsert" parameterType="java.util.List">
    insert into chat_history_picture (id, chat_history_id, picture_id, picture_type, sort_order, create_time, is_delete)
    values
    <foreach collection="list" item="item" separator=",">
        (#{item.id}, #{item.chatHistoryId}, #{item.pictureId}, #{item.pictureType}, #{item.sortOrder}, #{item.createTime}, #{item.isDelete})
    </foreach>
</insert>
```

---

## 影响分析

### 1. 查询操作
MyBatis-Plus 会自动在查询时添加 `is_delete = 0` 条件,无需手动修改现有查询代码。

### 2. 删除操作
使用 `remove()` 或 `removeById()` 方法会自动执行逻辑删除 (UPDATE is_delete = 1),无需手动修改。

### 3. 插入操作
新插入的记录 `is_delete` 默认为 0,无需手动设置。

---

## 回滚脚本

如果需要回滚变更:

```sql
-- 删除索引
ALTER TABLE chat_history_picture DROP INDEX idx_is_delete;

-- 删除字段
ALTER TABLE chat_history_picture DROP COLUMN is_delete;
```

---

## 执行步骤

1. **备份数据库**
   ```bash
   mysqldump -u root -p yun_picture > backup_before_migration.sql
   ```

2. **在测试环境执行 SQL**
   ```sql
   USE yun_picture_test;
   source migration/add_chat_history_picture_is_delete.sql;
   ```

3. **验证变更**
   ```sql
   DESC chat_history_picture;
   SELECT * FROM chat_history_picture LIMIT 1;
   ```

4. **更新实体类和 Mapper XML**

5. **运行单元测试**
   ```bash
   mvn test -Dtest=ChatHistoryPictureServiceTest
   ```

6. **在生产环境执行** (确认测试通过后)

---

## 注意事项

⚠️ **重要提示:**
- 执行前务必备份数据库
- 先在测试环境验证
- 确保应用程序停止或处于维护模式
- 执行后立即验证数据完整性
