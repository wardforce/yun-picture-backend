package com.wuzhenhua.yunpicturebackend.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "PictureUploadRequest", description = "图片上传请求DTO")
public class PictureUploadRequest implements java.io.Serializable{
    @Schema(description = "图片 id", example = "123", type = "long")
    private Long id;
    private static final long serialVersionUID = 1L;
}
