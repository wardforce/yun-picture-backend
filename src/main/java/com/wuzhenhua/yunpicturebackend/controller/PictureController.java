package com.wuzhenhua.yunpicturebackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzhenhua.yunpicturebackend.annotation.AuthCheck;
import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.common.DeleteRequest;
import com.wuzhenhua.yunpicturebackend.constant.UserConstant;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.picture.*;
import com.wuzhenhua.yunpicturebackend.model.entity.Picture;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.enums.PictureReviewStatusEnum;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureTagCategory;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import com.wuzhenhua.yunpicturebackend.service.PictureService;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ResultUtils;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Tag(name = "PictureController", description = "图片控制器")
@Slf4j
@RestController
@RequestMapping("/picture")
public class PictureController {
    @Resource
    private UserService userService;
    @Resource
    private PictureService pictureService;
    private User loginUser;

    /**
     * 上传图片（可重新上传）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "上传图片（可重新上传）", description = "已存在同名时覆盖更新")
    @Parameter(name = "file", description = "图片文件")
    @Parameter(name = "pictureUploadRequest", description = "图片上传请求体中的元信息，如名称/标签/分类等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        log.info("uploadPicture loginUserId={}", loginUser.getId());
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
    /**
     * 通过url上传图片（可重新上传）
     */
    @PostMapping(value = "/upload/url")
//    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "通过url上传图片（可重新上传）", description = "已存在同名时覆盖更新")
    @Parameter(name = "pictureUploadRequest", description = "图片上传请求体中的元信息，如名称/标签/分类等")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        log.info("uploadPicture loginUserId={}", loginUser.getId());
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 删除图片
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除图片", description = "仅本人或管理员可删除指定图片")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
            @ApiResponse(responseCode = "50001", description = "操作失败"),
    })
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long id = deleteRequest.getId();
        //判断是否存在
        Picture picture = pictureService.getById(id);
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        //仅本人和管理员可以删除
        ThrowUtils.throwIf(!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser),
                ErrorCode.NO_AUTH_ERROR);
        boolean result = pictureService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片（仅管理员可用）
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "更新图片（仅管理员）", description = "管理员更新图片的名称、描述、标签等信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
            @ApiResponse(responseCode = "50001", description = "操作失败"),
    })
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        //补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        //操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取图片（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "根据 id 获取图片（仅管理员）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
    })
    public BaseResponse<Picture> getPictureById(@Parameter(description = "图片ID", required = true) long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(picture);
    }

    /**
     * 根据 id 获取图片（封装类）
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据 id 获取图片（VO）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
    })
    public BaseResponse<PictureVO> getPictureVOById(@Parameter(description = "图片ID", required = true) long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVO(picture, request));
    }

    /**
     * 分页获取图片列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "分页获取图片列表（仅管理员）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
    })
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片列表（封装类）
     */
    @PostMapping("/list/page/vo")
    @Operation(summary = "分页获取图片列表（VO）", description = "限制单页大小不超过20")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
    })
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest,
                                                             HttpServletRequest request) {
        long current = pictureQueryRequest.getCurrent();
        long size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //普通用户默认只能看到审核通过的数据
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        // 查询数据库
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size),
                pictureService.getQueryWrapper(pictureQueryRequest));
        // 获取封装类
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    @Operation(summary = "编辑图片", description = "仅本人或管理员可以编辑图片")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
            @ApiResponse(responseCode = "50001", description = "操作失败"),
    })
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        // 注意将 list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());
        // 数据校验
        pictureService.validPicture(picture);
        User loginUser = userService.getLoginUser(request);
        //补充审核参数
        pictureService.fillReviewParams(picture, loginUser);
        // 判断是否存在
        long id = pictureEditRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    @Operation(summary = "获取图片标签与分类枚举", description = "返回前端可用的标签和分类列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40100", description = "未登录"),
            @ApiResponse(responseCode = "40300", description = "无权限"),

    })
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意", "二次元");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报", "二次元");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/review")
    @Operation(summary = "审核图片", description = "审核图片")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40400",description = "图片不存在"),
            @ApiResponse(responseCode = "50001",description = "操作失败"),
    })
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload/batch")
    @Operation(summary = "批量上传图片,仅仅管理员可用", description = "批量上传图片")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40400",description = "图片不存在"),
            @ApiResponse(responseCode = "50001",description = "操作失败"),
    })
    public BaseResponse<Integer> uploadPictureByBatch(@RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatche(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }

}
