package com.wuzhenhua.yunpicturebackend.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "空间分类分析响应")
public class SpaceCategoryAnalyzeResponse implements Serializable {

    /**
     * 图片分类
     */
    @Schema(description = "图片分类")
    private String category;

    /**
     * 图片数量
     */
    @Schema(description = "图片数量")
    private Long count;

    /**
     * 分类图片总大小
     */
    @Schema(description = "分类图片总大小")
    private Long totalSize;

    private static final long serialVersionUID = 1L;
}
