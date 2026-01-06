package com.wuzhenhua.yunpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.service.ChatHistoryService;
import com.wuzhenhua.yunpicturebackend.mapper.ChatHistoryMapper;
import org.springframework.stereotype.Service;

/**
 * @author ward
 * @description 针对表【chat_history(对话历史)】的数据库操作Service实现
 * @createDate 2026-01-05 16:09:41
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
        implements ChatHistoryService {

    @Override
    public ChatHistory saveUserMessage(Long userId, String message, Long pictureId, Long sessionId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType("user");
        chatHistory.setPictureId(pictureId);
        // 如果没有传入 sessionId，则使用雪花算法自动生成
        chatHistory.setSessionId(sessionId != null ? sessionId : IdWorker.getId());
        this.save(chatHistory);
        return chatHistory;
    }

    @Override
    public ChatHistory saveAiMessage(Long userId, String message, Long pictureId, Long sessionId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType("ai");
        chatHistory.setPictureId(pictureId);
        // 如果没有传入 sessionId，则使用雪花算法自动生成
        chatHistory.setSessionId(sessionId != null ? sessionId : IdWorker.getId());
        this.save(chatHistory);
        return chatHistory;
    }
}
