package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;

/**
 * 用户注册请求DTO
 */
@Data
@Schema(description = "用户注册请求DTO")
public class UserRegisterRequest implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 8733563954485117742L;
    /**
     * 用户账号
     * 用于标识用户的唯一登录名
     */
    private String userAccount;

    /**
     * 用户密码
     * 用于用户登录时的身份验证
     */
    private String userPassword;

    /**
     * 确认密码
     * 用于验证用户输入的密码是否一致
     */
    private String checkPassword;
}
