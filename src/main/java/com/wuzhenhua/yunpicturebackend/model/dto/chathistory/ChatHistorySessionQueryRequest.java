package com.wuzhenhua.yunpicturebackend.model.dto.chathistory;

import com.wuzhenhua.yunpicturebackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 会话列表查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "ChatHistorySessionQueryRequest", description = "会话列表查询请求DTO")
public class ChatHistorySessionQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（精确查询）
     */
    @Schema(description = "会话ID，用于精确查询")
    private Long sessionId;

    /**
     * 用户ID（仅管理员可用）
     */
    @Schema(description = "用户ID，仅管理员查询时可用")
    private Long userId;
    @Schema(description = "空间 id")
    private Long spaceId;
    /**
     * 时间段开始
     */
    @Schema(description = "查询时间段开始")
    private Date startTime;

    /**
     * 时间段结束
     */
    @Schema(description = "查询时间段结束")
    private Date endTime;
}
