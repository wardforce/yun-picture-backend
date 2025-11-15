package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(name = "UserAddRequest", description = "创建用户的请求体")
public class UserAddRequest implements Serializable {

    @Schema(description = "用户昵称", example = "小明", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 30)
    private String userName;

    @Schema(description = "账号", example = "xiaoming001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userAccount;

    @Schema(description = "用户头像 URL")
    private String userAvatar;

    @Schema(description = "用户简介", example = "一个热爱编程的学生", maxLength = 200)
    private String userProfile;

    @Schema(description = "用户角色", example = "user", allowableValues = {"user", "admin", "vip"})
    private String userRole;

    @Schema(description = "VIP 编号")
    private Long vipNumber;
    private String vipLevel;

    // 强烈建议手机号用 String 类型以保留前导 0、便于做长度/正则校验
    @Schema(description = "手机号", example = "13800138000", pattern = "^\\d{6,20}$")
    private Long phoneNumber; // 若可改动，建议改为 String

    @Schema(description = "邮箱", example = "xiaoming@example.com", format = "email")
    private String email;

    @Schema(description = "手机国家码", example = "+86")
    private String phoneCountryCode;

    @Serial
    private static final long serialVersionUID = 1L;
}