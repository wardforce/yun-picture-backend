package com.wuzhenhua.yunpicturebackend.controller;

import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.utils.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "MainController", description = "系统主要控制器")
@RestController
@RequestMapping("/")
public class MainController {

    /**
     * 健康检查
     *
     * @return 健康状态
     */

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查系统是否运行正常")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
            @ApiResponse(responseCode = "50001", description = "操作失败"),
            @ApiResponse(responseCode = "40100", description = "未登录"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "40300", description = "禁止访问"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
    })
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
