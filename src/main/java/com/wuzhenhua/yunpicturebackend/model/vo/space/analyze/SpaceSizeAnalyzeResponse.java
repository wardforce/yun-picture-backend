package com.wuzhenhua.yunpicturebackend.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "空间图片大小分析响应")
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 图片大小范围
     */
    @Schema(description = "图片大小范围", example = "0-100KB")
    private String sizeRange;

    /**
     * 图片数量
     */
    @Schema(description = "图片数量", example = "128")
    private Long count;

    private static final long serialVersionUID = 1L;
}

