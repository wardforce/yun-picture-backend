package com.wuzhenhua.yunpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 对话历史
 * 
 * @TableName chat_history
 */
@TableName(value = "chat_history")
@Data
public class ChatHistory implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息
     */
    private String message;

    /**
     * 消息类型 user/ai
     */
    private String messageType;

    /**
     * 原图 ID
     */
    private Long pictureId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @Schema(description = "是否删除(0-未删除, 1-已删除)")
    private Integer isDelete;

    /**
     * 对话 ID（用于标识同一轮对话）
     */
    private Long sessionId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}