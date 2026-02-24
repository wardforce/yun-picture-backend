package com.wuzhenhua.yunpicturebackend.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "空间分析请求")
public class SpaceAnalyzeRequest implements Serializable {

    @Schema(description = "空间 ID")
    private Long spaceId;

    @Schema(description = "是否查询公共图库")
    private boolean queryPublic;

    @Schema(description = "全空间分析")
    private boolean queryAll;

    private static final long serialVersionUID = 1L;
}
