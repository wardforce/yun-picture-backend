package com.wuzhenhua.yunpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户
 * &#064;TableName  user
 */
@TableName(value ="user")
@Data
@Schema(description = "用户实体类")
public class User {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "用户 id")
    private Long id;

    /**
     * 账号
     */
//    @TableField("user_account")
    @Schema(description = "用户账号",example = "users",requiredMode = Schema.RequiredMode.REQUIRED)
    private String userAccount;

    /**
     * 密码
     */
    @Schema(description = "用户密码",example = "12345678",requiredMode = Schema.RequiredMode.REQUIRED)
    private String userPassword;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称",example = "wuzhenhua")
    private String userName;
    @Schema(description = "minio对应桶")
    private String userAvatarBucket;
    @Schema(description = "minio对象名称")
    private String userAvatarObject;
    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @Schema(description = "用户角色")
    private String userRole;

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

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("is_delete")
    private Integer isDelete;

    /**
     * 会员过期时间
     */
    @Schema(description = "会员过期时间")
    private Date vipExpireTime;

    /**
     * 会员兑换码
     */
    @Schema(description = "会员兑换码")
    private String vipCode;

    /**
     * 会员编号
     */
    @Schema(description = "会员编号")
    private Long vipNumber;

    /**
     * 分享码
     */
    @Schema(description = "分享码")
    private String shareCode;

    /**
     * 邀请用户 id
     */
    @Schema(description = "邀请用户 id")
    private Long inviteUser;

    /**
     * 手机号
     */
    @TableField(fill = FieldFill.UPDATE)
    //注释效果：当前端设置为空数值时，自动设置为空
    @Schema(description = "手机号")
    private Long phoneNumber;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 国家代码
     */
    @Schema(description = "国家代码")
    private String phoneCountryCode;
    @Schema(description = "会员等级",example = "standard",format="standard,pro,max")
    private String vipLevel;

}