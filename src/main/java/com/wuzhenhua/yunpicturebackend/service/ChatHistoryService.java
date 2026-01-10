package com.wuzhenhua.yunpicturebackend.service;

import com.wuzhenhua.yunpicturebackend.model.dto.chathistory.ChatHistoryDetailQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.chathistory.ChatHistorySessionQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistoryDetailVO;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistorySessionVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author ward
 * @description 针对表【chat_history(对话历史)】的数据库操作Service
 * @createDate 2026-01-05 16:09:41
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存用户消息（单图片，向后兼容）
     */
    ChatHistory saveUserMessage(Long userId, String message, Long pictureId, Long sessionId);

    /**
     * 保存用户消息（多图片）
     *
     * @param userId     用户 ID
     * @param message    消息内容（prompt）
     * @param pictureIds 上传图片 ID 列表（最多14张）
     * @param sessionId  对话 ID（可选，未传入则自动生成）
     * @return 保存的 ChatHistory
     */
    ChatHistory saveUserMessage(Long userId, String message, java.util.List<Long> pictureIds, Long sessionId);
    /**
     * 保存用户消息（多图片）
     *
     * @param userId     用户 ID
     * @param message    消息内容（prompt）
     * @param pictureIds 上传图片 ID 列表（最多14张）
     * @param sessionId  对话 ID（可选，未传入则自动生成）
     * @param spaceId  空间 ID
     * @return 保存的 ChatHistory
     */
    ChatHistory saveUserMessage(Long userId, String message, java.util.List<Long> pictureIds, Long sessionId, Long spaceId);

    /**
     * 保存 AI 消息（单图片，向后兼容）
     */
    ChatHistory saveAiMessage(Long userId, String message, Long pictureId, Long sessionId);

    /**
     * 保存 AI 消息（多图片）
     *
     * @param userId     用户 ID
     * @param message    AI 响应文本
     * @param pictureIds 生成图片 ID 列表
     * @param sessionId  对话 ID
     * @return 保存的 ChatHistory
     */
    ChatHistory saveAiMessage(Long userId, String message, java.util.List<Long> pictureIds, Long sessionId);

    /**
     * 保存 AI 消息（多图片）
     *
     * @param userId     用户 ID
     * @param message    AI 响应文本
     * @param pictureIds 生成图片 ID 列表
     * @param sessionId  对话 ID
     * @return 保存的 ChatHistory
     */
    ChatHistory saveAiMessage(Long userId, String message, java.util.List<Long> pictureIds, Long sessionId, Long spaceId);
    // ==================== 新增管理功能 ====================

    /**
     * 分页查询会话摘要列表
     *
     * @param queryRequest  查询条件
     * @param includeUserId 是否包含userId字段（管理员接口需要）
     * @return 会话摘要分页结果
     */
    Page<ChatHistorySessionVO> listSessionSummary(ChatHistorySessionQueryRequest queryRequest, boolean includeUserId);

    /**
     * 分页查询会话详情
     *
     * @param queryRequest 查询条件
     * @param request      HttpServletRequest
     * @return 会话详情分页结果
     */
    Page<ChatHistoryDetailVO> listSessionDetail(ChatHistoryDetailQueryRequest queryRequest,
                                                jakarta.servlet.http.HttpServletRequest request);

    /**
     * 根据会话ID删除所有相关记录（级联删除）
     *
     * @param sessionId 会话ID
     * @return 是否删除成功
     */
    boolean deleteBySessionId(Long sessionId);

    /**
     * 根据记录ID删除单条记录（级联删除关联图片）
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    boolean deleteByIdWithCascade(Long id);

    /**
     * 检查会话是否属于指定用户
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 是否属于
     */
    boolean isSessionOwnedByUser(Long sessionId, Long userId);

    /**
     * 检查记录是否属于指定用户
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 是否属于
     */
    boolean isRecordOwnedByUser(Long id, Long userId);
}
