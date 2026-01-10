package com.wuzhenhua.yunpicturebackend.model.dto.chathistory;

import com.wuzhenhua.yunpicturebackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 会话详情查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "ChatHistoryDetailQueryRequest", description = "会话详情查询请求DTO")
public class ChatHistoryDetailQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID（必填）
     */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "会话ID，必填参数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;

    /**
     * 消息类型过滤（user/ai）
     */
    @Schema(description = "消息类型过滤，可选值：user、ai")
    private String messageType;

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
