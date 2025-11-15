package com.wuzhenhua.yunpicturebackend.model.dto.user;

import com.wuzhenhua.yunpicturebackend.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户查询请求DTO")
public class UserQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    @Schema(description = "用户id")
    private Long id;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String userName;

    /**
     * 账号
     */
    @Schema(description = "用户账号")
    private String userAccount;

    /**
     * 简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @Schema(description = "用户角色")
    private String userRole;

    @Schema(description = "用户vip编号")
    private Long vipNumber;

    @Schema(description = "用户vip等级")
    private String vipLevel;

    @Schema(description = "会员过期时间")
    private Date vipExpireTime;
    @Schema(description = "邀请人id")
    private Long inviteUser;
    @Schema(description = "分享码")
    private String shareCode;
    /**
     *
     */
    @Schema(description = "手机号")
    private Long phoneNumber;
    @Schema(description = "会员兑换码")
    private String vipCode;
    /**
     *
     */
    @Schema(description = "邮箱")
    private String email;


    /**
     *
     */
    @Schema(description = "手机国家代码")
    private String phoneCountryCode;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    private Date editTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;
    @Schema(description = "更新时间")
    private Date updateTime;


    private static final long serialVersionUID = 1L;
}

