package com.wuzhenhua.yunpicturebackend.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "空间占用情况分析请求DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUsageAnalyzeRequest extends SpaceAnalyzeRequest {
}
