package com.wuzhenhua.yunpicturebackend.model.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "PictureTagCategory", description = "图片标签分类VO，用于返回图片的标签列表与分类")
public class PictureTagCategory {
    @Schema(description = "图片标签列表", example = "[\"自然\",\"风景\",\"山川\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "array")
    private List<String> tagList;
    @Schema(description = "图片分类", example = "自然", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "string")
    private List<String> categoryList;

}
