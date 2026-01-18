package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "邮箱验证码重置密码请求")
public class EmailResetPasswordRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "验证码")
    private String code;

    @Schema(description = "新密码")
    private String newPassword;

    @Schema(description = "确认密码")
    private String checkPassword;
}
