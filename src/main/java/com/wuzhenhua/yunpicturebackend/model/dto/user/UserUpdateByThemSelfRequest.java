package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户更新请求DTO")
public class UserUpdateByThemSelfRequest{
    @Schema(description = "用户id", required = true)
    private Long id;
    @Schema(description = "用户昵称")
    private String userName;
    @Schema(description = "用户简介")
    private String userProfile;
    @Schema(description = "手机号")
    private Long phoneNumber;
    @Schema(description = "邮箱")
    private String email;
}
