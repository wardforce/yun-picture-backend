package com.wuzhenhua.yunpicturebackend.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "空间用户分析请求")
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {

    /**
     * 用户 ID
     */
    @Schema(description = "用户ID", example = "1234567890")
    private Long userId;

    /**
     * 时间维度：day / week / month
     */
    @Schema(description = "时间维度", example = "day", allowableValues = {"day", "week", "month"})
    private String timeDimension;
}
