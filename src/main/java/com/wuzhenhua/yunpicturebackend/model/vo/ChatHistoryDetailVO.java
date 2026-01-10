package com.wuzhenhua.yunpicturebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 会话详情视图对象
 */
@Data
@Schema(name = "ChatHistoryDetailVO", description = "会话详情视图对象")
public class ChatHistoryDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @Schema(description = "聊天记录ID")
    private Long id;

    /**
     * 消息内容
     */
    @Schema(description = "消息内容")
    private String message;

    /**
     * 消息类型
     */
    @Schema(description = "消息类型：user/ai")
    private String messageType;

    /**
     * 图片ID（主图片，向后兼容）
     */
    @Schema(description = "主图片ID")
    private Long pictureId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private Long sessionId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    @Schema(description = "空间 id")
    private Long spaceId;

    /**
     * 关联的图片列表
     */
    @Schema(description = "关联的图片列表")
    private List<ChatHistoryPictureVO> pictures;
}
