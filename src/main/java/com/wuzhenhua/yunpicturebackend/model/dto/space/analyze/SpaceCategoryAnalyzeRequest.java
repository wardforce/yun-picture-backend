package com.wuzhenhua.yunpicturebackend.model.dto.space.analyze;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "空间分类分析请求DTO")
public class SpaceCategoryAnalyzeRequest extends SpaceAnalyzeRequest {

}
