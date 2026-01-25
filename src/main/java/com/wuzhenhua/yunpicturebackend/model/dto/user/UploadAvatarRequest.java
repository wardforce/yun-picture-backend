package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "用户头像URL上传请求")
@Data
public class UploadAvatarRequest implements Serializable {

    @Schema(description = "头像文件URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/avatar.jpg")
    private String fileUrl;

    private static final long serialVersionUID = 1L;
}
