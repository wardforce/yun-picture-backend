package com.wuzhenhua.yunpicturebackend.service;

import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author ward
 * @description 针对表【chat_history(对话历史)】的数据库操作Service
 * @createDate 2026-01-05 16:09:41
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存用户消息
     * 
     * @param userId    用户 ID
     * @param message   消息内容（prompt）
     * @param pictureId 上传图片 ID（可选）
     * @param sessionId 对话 ID（可选，未传入则自动生成）
     * @return 保存的 ChatHistory
     */
    ChatHistory saveUserMessage(Long userId, String message, Long pictureId, Long sessionId);

    /**
     * 保存 AI 消息
     * 
     * @param userId    用户 ID
     * @param message   AI 响应文本
     * @param pictureId 生成图片 ID（可选）
     * @param sessionId 对话 ID（可选，未传入则自动生成）
     * @return 保存的 ChatHistory
     */
    ChatHistory saveAiMessage(Long userId, String message, Long pictureId, Long sessionId);
}
