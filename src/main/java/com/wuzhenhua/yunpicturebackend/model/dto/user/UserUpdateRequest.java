package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

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
    private Long phoneNumber;

    /**
     *
     */
    private String email;

    private String vipLevel;

    /**
     *
     */
    private String phoneCountryCode;
    @Serial
    private static final long serialVersionUID = 1L;
}
