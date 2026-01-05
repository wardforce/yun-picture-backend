package com.wuzhenhua.yunpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 对话历史
 * 
 * @TableName chat_history
 */
@TableName(value = "chat_history")
@Data
@Schema(description = "对话历史实体类")
public class ChatHistory {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "id")
    private Long id;

    /**
     * 消息
     */
    @Schema(description = "消息")
    private String message;

    /**
     * 消息类型 user/ai
     */
    @Schema(description = "消息类型 user/ai")
    private String messageType;

    /**
     * 原图 ID
     */
    @Schema(description = "原图 ID")
    private Long uploadPictureId;

    /**
     * 生成图 ID
     */
    @Schema(description = "生成图 ID")
    private Long generatePictureId;

    /**
     * 创建用户 id
     */
    @Schema(description = "创建用户 id")
    private Long userId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @Schema(description = "是否删除")
    private Integer isDelete;
}
