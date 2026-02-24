package com.wuzhenhua.yunpicturebackend.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "空间使用分析响应")
public class SpaceUsageAnalyzeResponse implements Serializable {

    @Schema(description = "已使用大小")
    private Long usedSize;

    @Schema(description = "总大小")
    private Long maxSize;

    @Schema(description = "空间使用比例")
    private Double sizeUsageRatio;

    @Schema(description = "当前图片数量")
    private Long usedCount;

    @Schema(description = "最大图片数量")
    private Long maxCount;

    @Schema(description = "图片数量占比")
    private Double countUsageRatio;

    private static final long serialVersionUID = 1L;
}
