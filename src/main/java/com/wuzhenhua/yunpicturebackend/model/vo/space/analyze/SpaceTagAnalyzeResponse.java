package com.wuzhenhua.yunpicturebackend.model.vo.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "空间标签分析响应")
public class SpaceTagAnalyzeResponse implements Serializable {

    /**
     * 标签名称
     */
    @Schema(description = "标签名称", example = "风景")
    private String tag;

    /**
     * 使用次数
     */
    @Schema(description = "使用次数", example = "42")
    private Long count;

    private static final long serialVersionUID = 1L;
}
