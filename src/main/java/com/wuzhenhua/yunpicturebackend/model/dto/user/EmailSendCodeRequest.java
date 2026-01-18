package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "发送邮箱验证码请求")
public class EmailSendCodeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "验证码类型: LOGIN/RESET_PASSWORD")
    private String codeType;
}
