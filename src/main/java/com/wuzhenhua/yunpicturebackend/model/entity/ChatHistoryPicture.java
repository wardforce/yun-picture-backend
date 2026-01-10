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
 * 对话历史图片关联
 *
 * @TableName chat_history_picture
 */
@TableName(value = "chat_history_picture")
@Data
public class ChatHistoryPicture implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 对话历史ID
     */
    private Long chatHistoryId;

    /**
     * 图片ID
     */
    private Long pictureId;

    /**
     * 图片类型: INPUT/OUTPUT
     */
    private String pictureType;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    @TableLogic
    @Schema(description = "是否删除(0-未删除, 1-已删除)")
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
