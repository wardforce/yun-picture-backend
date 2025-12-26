package com.wuzhenhua.yunpicturebackend.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户密码更新")
public class UserUpdatePasswordRequest {

    String userOldPassword;
    String userNewPassword;
    String checkPassword;
}
