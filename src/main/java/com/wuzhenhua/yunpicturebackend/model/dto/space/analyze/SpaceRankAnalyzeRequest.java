package com.wuzhenhua.yunpicturebackend.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "空间排名分析请求")
public class SpaceRankAnalyzeRequest implements Serializable {

    /**
     * 排名前 N 的空间
     */
    @Schema(description = "排名前 N 的空间", example = "10")
    private Integer topN = 10;

    private static final long serialVersionUID = 1L;
}
