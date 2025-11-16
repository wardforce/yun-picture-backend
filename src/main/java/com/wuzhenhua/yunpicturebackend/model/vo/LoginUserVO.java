package com.wuzhenhua.yunpicturebackend.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 已登录用户视图（脱敏）
 *
 * @TableName user
 */

@Data
public class LoginUserVO implements Serializable {
    /**
     * id
     */

    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 编辑时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date editTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 会员过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date vipExpireTime;
    /**
     * 会员编号
     */
    @Schema(description = "会员编号")
    private Long vipNumber;

    @Schema(description = "会员等级")
    private String vipLevel;
    /**
     * 邀请用户 id
     */
    private Long inviteUser;

    /**
     *
     */
    private Long phoneNumber;

    /**
     *
     */
    private String email;

    /**
     *
     */
    private String phoneCountryCode;
}