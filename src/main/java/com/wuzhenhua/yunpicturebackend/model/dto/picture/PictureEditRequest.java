package com.wuzhenhua.yunpicturebackend.model.dto.picture;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@Schema(name = "PictureEditRequest", description = "图片编辑请求DTO，用于修改图片的名称、简介、分类与标签")
public class PictureEditRequest implements Serializable {
    /**
     * id
     */
    @Schema(description = "图片id", requiredMode = Schema.RequiredMode.REQUIRED, type = "long")
    private Long id;

    @Schema(description = "图片名称", example = "美丽风景.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "string")
    private String name;

    @Schema(description = "图片简介/描述", example = "这是一张美丽的风景照片", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "string")
    private String introduction;

    @Schema(description = "图片分类", example = "风景", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "string")
    private String category;

    @Schema(description = "图片标签列表", example = "[\"自然\",\"风景\",\"山川\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED, type = "array")
    private List<String> tags;

    private static final long serialVersionUID = 1L;


}
