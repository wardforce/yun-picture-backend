package com.wuzhenhua.yunpicturebackend.model.vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 会员过期时间
     */
    private Date vipExpireTime;
    /**
     * 会员编号
     */
    private Long vipNumber;
    /**
     * 邀请用户 id
     */
    private Long inviteUser;

    /**
     *
     */
    private Integer phoneNumber;

    /**
     *
     */
    private String email;

    /**
     *
     */
    private String phoneCountryCode;
}