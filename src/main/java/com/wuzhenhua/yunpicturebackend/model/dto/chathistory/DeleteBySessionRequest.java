package com.wuzhenhua.yunpicturebackend.model.dto.chathistory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 按会话删除请求
 */
@Data
@Schema(name = "DeleteBySessionRequest", description = "按会话删除请求DTO")
public class DeleteBySessionRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    @Schema(description = "要删除的会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sessionId;
}
