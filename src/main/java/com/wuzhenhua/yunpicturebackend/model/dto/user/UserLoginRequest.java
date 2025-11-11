package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

@Data
@Schema(description = "用户登录请求DTO")
public class UserLoginRequest implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 8733563954485117742L;
    /**
     * 用户账号
     * 用于标识用户的唯一登录名
     */
    @Schema(description = "用户账号")
    private String userAccount;

    /**
     * 用户密码
     * 用于用户登录时的身份验证
     */
    @Schema(description = "用户密码")
    private String userPassword;
}


