package com.wuzhenhua.yunpicturebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会话摘要视图对象
 */
@Data
@Schema(name = "ChatHistorySessionVO", description = "会话摘要视图对象")
public class ChatHistorySessionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private Long sessionId;

    /**
     * 最早聊天时间
     */
    @Schema(description = "最早聊天时间")
    private Date firstChatTime;

    /**
     * 第一条prompt
     */
    @Schema(description = "第一条用户消息（prompt）")
    private String firstPrompt;

    /**
     * 用户ID（仅管理员接口返回）
     */
    @Schema(description = "用户ID，仅管理员接口返回")
    private Long userId;
    @Schema(description = "空间 id")
    private Long spaceId;
}
