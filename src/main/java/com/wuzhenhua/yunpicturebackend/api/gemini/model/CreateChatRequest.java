package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CreateChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户提示词
     */
    private String prompt;

    /**
     * 输入图片ID列表（最多14张）
     */
    private List<Long> pictureIds;

    /**
     * 对话 ID（可选，用于继续之前的对话）
     */
    private Long sessionId;
    @Schema(description = "空间 id,可选")
    private Long spaceId;

}
