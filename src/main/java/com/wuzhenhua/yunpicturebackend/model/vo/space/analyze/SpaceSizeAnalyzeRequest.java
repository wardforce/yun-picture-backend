package com.wuzhenhua.yunpicturebackend.model.vo.space.analyze;

import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "空间图片大小分析请求")
public class SpaceSizeAnalyzeRequest extends SpaceAnalyzeRequest {

}
