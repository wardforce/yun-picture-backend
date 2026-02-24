package com.wuzhenhua.yunpicturebackend.controller;

import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.SpaceCategoryAnalyzeRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.SpaceTagAnalyzeRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.SpaceUsageAnalyzeRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.SpaceUserAnalyzeRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.space.analyze.*;
import com.wuzhenhua.yunpicturebackend.service.SpaceAnalyzeService;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ResultUtils;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "SpaceAnalyzeController", description = "图片分析")
@Slf4j
@RestController
@RequestMapping("/space/analyze")
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    private UserService userService;

    /**
     * 获取空间使用状态
     */
    @Operation(summary = "获取空间使用状态", description = "查询指定空间的使用情况，包括已用容量、剩余容量、使用百分比等信息")
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeRequest> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeRequest spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(loginUser, spaceUsageAnalyzeRequest);
        return ResultUtils.success(spaceUsageAnalyze);
    }
    /**
     * 获取空间分类统计
     */
    @Operation(summary = "获取空间分类统计", description = "按图片分类统计空间使用情况，返回各分类的图片数量和占用容量")
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(@RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }

    /**
     * 获取空间标签分析
     */
    @Operation(summary = "获取空间标签分析", description = "统计空间内图片标签的使用频率，返回各标签的使用次数")
    @PostMapping("/tag")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(@RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }
    @PostMapping("/size")
    @Operation(summary = "用户上传图片大小分析")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(@RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }
    @PostMapping("/user")
    @Operation(summary = "空间用户分析", description = "统计空间内用户的活跃度，返回各用户的上传图片数量和最后一次上传时间")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> resultList = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(resultList);
    }
}
