package com.wuzhenhua.yunpicturebackend.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "空间标签分析请求")
public class SpaceTagAnalyzeRequest extends SpaceAnalyzeRequest {

}
