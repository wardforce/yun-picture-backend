package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Schema(description = "管理员用户更新请求DTO")
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Schema(description = "用户角色")
    private String userRole;
    @Schema(description = "用户vip编号")
    private Long vipNumber;
    /**
     *
     */
    @Schema(description = "用户手机号")
    private String phoneNumber;

    /**
     *
     */
    @Schema(description = "用户邮箱")
    private String email;

    @Schema(description = "用户vip等级")
    private String vipLevel;

    /**
     *
     */
    @Schema(description = "用户手机号国家代码")
    private String phoneCountryCode;

    @Schema(description = "会员过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+8")
    private Date vipExpireTime;

    @Serial
    private static final long serialVersionUID = 1L;
}
