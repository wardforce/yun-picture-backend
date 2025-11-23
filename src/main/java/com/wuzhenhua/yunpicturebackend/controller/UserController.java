package com.wuzhenhua.yunpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzhenhua.yunpicturebackend.annotation.AuthCheck;
import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.common.DeleteRequest;
import com.wuzhenhua.yunpicturebackend.constant.UserConstant;
import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import com.wuzhenhua.yunpicturebackend.model.dto.user.*;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.LoginUserVO;
import com.wuzhenhua.yunpicturebackend.model.vo.UserVO;
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
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "UserController", description = "用户相关接口")
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @return 注册状态
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        Long result = userService.userRegister(
                userRegisterRequest.getUserAccount(),
                userRegisterRequest.getUserPassword(),
                userRegisterRequest.getCheckPassword()
        );
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @return 登录状态
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "传入用户账号与密码进行登录")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    @Operation(summary = "获取当前登录用户", description = "获取当前会话中的登录用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40100", description = "未登录"),
    })
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userService.getLoginUserVO(userService.getLoginUser(request)));
    }

    @GetMapping("/logout")
    @Operation(summary = "用户退出", description = "退出当前登录会话")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40100", description = "未登录"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean loginUserVO = userService.userLogOut(request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 创建用户
     *
     * @return 注册状态
     */
    @PostMapping("/add")
    @Operation(summary = "创建用户", description = "管理员创建新用户（默认密码：12345678）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50001", description = "操作失败"),
    })
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);
        final String DEFAULT_PASSWORD = "12345678";
        user.setUserPassword(DEFAULT_PASSWORD);
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @Operation(summary = "根据 id 获取用户（仅管理员）", description = "管理员根据 id 获取用户详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
    })
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(@Parameter(description = "用户ID", required = true) @RequestParam("id") long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    @Operation(summary = "根据 id 获取用户包装类", description = "普通用户根据 id 获取包装类（内部复用管理员接口）")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "40400", description = "请求数据不存在"),
    })
    public BaseResponse<UserVO> getUserVOById(@Parameter(description = "用户ID", required = true) @RequestParam("id") long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "管理员删除用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50001", description = "操作失败"),
    })
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    @Operation(summary = "更新用户", description = "管理员更新用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50001", description = "操作失败"),
    })
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户
     *
     * @param userQueryRequest
     * @return
     */
    @Operation(summary = "分页获取用户（VO）", description = "分页查询用户并返回 UserVO 列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "0", description = "ok"),
            @ApiResponse(responseCode = "40000", description = "参数错误"),
            @ApiResponse(responseCode = "40101", description = "无权限"),
            @ApiResponse(responseCode = "50000", description = "系统内部异常"),
    })
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserPageVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        ThrowUtils.throwIf(current < 0 || pageSize < 0, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getUserQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

}
