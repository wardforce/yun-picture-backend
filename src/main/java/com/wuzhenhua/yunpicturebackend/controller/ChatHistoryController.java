package com.wuzhenhua.yunpicturebackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wuzhenhua.yunpicturebackend.common.BaseResponse;
import com.wuzhenhua.yunpicturebackend.common.DeleteRequest;

import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;

import com.wuzhenhua.yunpicturebackend.model.dto.chathistory.ChatHistoryDetailQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.chathistory.ChatHistorySessionQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.chathistory.DeleteBySessionRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.User;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistoryDetailVO;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistorySessionVO;
import com.wuzhenhua.yunpicturebackend.service.ChatHistoryService;
import com.wuzhenhua.yunpicturebackend.service.UserService;
import com.wuzhenhua.yunpicturebackend.utils.ResultUtils;
import com.wuzhenhua.yunpicturebackend.utils.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天历史管理控制器
 */
@RestController
@RequestMapping("/api/chat-history")
@Tag(name = "ChatHistoryController", description = "聊天历史管理接口")
@Slf4j
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    // ==================== 查询接口 ====================

    /**
     * 管理员查询所有用户的会话列表
     */
    @PostMapping("/session/list/admin")
    @Operation(summary = "管理员查询会话列表", description = "仅管理员可用，查询所有用户的会话列表")
    public BaseResponse<Page<ChatHistorySessionVO>> listSessionByAdmin(
            @RequestBody ChatHistorySessionQueryRequest request,
            HttpServletRequest httpServletRequest) {

        // 权限检查：仅管理员可用
        User loginUser = userService.getLoginUser(httpServletRequest);
        ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "仅管理员可访问");

        // 查询会话列表，包含userId
        Page<ChatHistorySessionVO> result = chatHistoryService.listSessionSummary(request, true);

        return ResultUtils.success(result);
    }

    /**
     * 用户查询自己的会话列表
     */
    @PostMapping("/session/list/my")
    @Operation(summary = "查询我的会话列表", description = "用户查询自己的会话列表")
    public BaseResponse<Page<ChatHistorySessionVO>> listMySession(
            @RequestBody ChatHistorySessionQueryRequest request,
            HttpServletRequest httpServletRequest) {

        User loginUser = userService.getLoginUser(httpServletRequest);

        // 强制设置为当前用户的ID，防止越权
        request.setUserId(loginUser.getId());

        // 查询会话列表，保留userId字段返回
        Page<ChatHistorySessionVO> result = chatHistoryService.listSessionSummary(request, true);

        return ResultUtils.success(result);
    }

    /**
     * 查询会话详情
     */
    @PostMapping("/session/detail")
    @Operation(summary = "查询会话详情", description = "管理员或会话所有者可查看")
    public BaseResponse<Page<ChatHistoryDetailVO>> getSessionDetail(
            @RequestBody @Valid ChatHistoryDetailQueryRequest request,
            HttpServletRequest httpServletRequest) {

        User loginUser = userService.getLoginUser(httpServletRequest);
        Long sessionId = request.getSessionId();

        // 权限检查：管理员或会话所有者
        boolean isAdmin = userService.isAdmin(loginUser);
        boolean isOwner = chatHistoryService.isSessionOwnedByUser(sessionId, loginUser.getId());

        ThrowUtils.throwIf(!isAdmin && !isOwner, ErrorCode.NO_AUTH_ERROR, "无权访问此会话");

        // 查询会话详情
        Page<ChatHistoryDetailVO> result = chatHistoryService.listSessionDetail(request, httpServletRequest);

        return ResultUtils.success(result);
    }

    // ==================== 删除接口 ====================

    /**
     * 删除整个会话
     */
    @PostMapping("/session/delete")
    @Operation(summary = "删除会话", description = "删除整个会话及其所有消息，管理员或会话所有者可操作")
    public BaseResponse<Boolean> deleteBySession(
            @RequestBody @Valid DeleteBySessionRequest request,
            HttpServletRequest httpServletRequest) {

        User loginUser = userService.getLoginUser(httpServletRequest);
        Long sessionId = request.getSessionId();

        // 权限检查：管理员或会话所有者
        boolean isAdmin = userService.isAdmin(loginUser);
        boolean isOwner = chatHistoryService.isSessionOwnedByUser(sessionId, loginUser.getId());

        ThrowUtils.throwIf(!isAdmin && !isOwner, ErrorCode.NO_AUTH_ERROR, "无权删除此会话");

        // 级联删除
        boolean result = chatHistoryService.deleteBySessionId(sessionId);

        log.info("用户 {} 删除了会话 {}, 结果: {}", loginUser.getId(), sessionId, result);

        return ResultUtils.success(result);
    }

    /**
     * 删除单条聊天记录
     */
    @PostMapping("/delete")
    @Operation(summary = "删除聊天记录", description = "删除单条聊天记录及其关联图片，管理员或记录所有者可操作")
    public BaseResponse<Boolean> deleteById(
            @RequestBody DeleteRequest request,
            HttpServletRequest httpServletRequest) {

        Long id = request.getId();
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "记录ID无效");

        User loginUser = userService.getLoginUser(httpServletRequest);

        // 权限检查：管理员或记录所有者
        boolean isAdmin = userService.isAdmin(loginUser);
        boolean isOwner = chatHistoryService.isRecordOwnedByUser(id, loginUser.getId());

        ThrowUtils.throwIf(!isAdmin && !isOwner, ErrorCode.NO_AUTH_ERROR, "无权删除此记录");

        // 级联删除
        boolean result = chatHistoryService.deleteByIdWithCascade(id);

        log.info("用户 {} 删除了聊天记录 {}, 结果: {}", loginUser.getId(), id, result);

        return ResultUtils.success(result);
    }
}
