package com.wuzhenhua.yunpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzhenhua.yunpicturebackend.model.dto.chathistory.ChatHistoryDetailQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.dto.chathistory.ChatHistorySessionQueryRequest;
import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistoryPicture;
import com.wuzhenhua.yunpicturebackend.model.entity.Picture;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistoryDetailVO;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistoryPictureVO;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistorySessionVO;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import com.wuzhenhua.yunpicturebackend.service.ChatHistoryPictureService;
import com.wuzhenhua.yunpicturebackend.service.ChatHistoryService;
import com.wuzhenhua.yunpicturebackend.mapper.ChatHistoryMapper;
import com.wuzhenhua.yunpicturebackend.service.PictureService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ward
 * @description 针对表【chat_history(对话历史)】的数据库操作Service实现
 * @createDate 2026-01-05 16:09:41
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
        implements ChatHistoryService {

    @Resource
    private ChatHistoryPictureService chatHistoryPictureService;

    @Resource
    private PictureService pictureService;

    @Override
    public ChatHistory saveUserMessage(Long userId, String message, Long pictureId, Long sessionId) {
        // 向后兼容：单图片转为列表
        List<Long> pictureIds = pictureId != null ? Collections.singletonList(pictureId) : null;
        return saveUserMessage(userId, message, pictureIds, sessionId);
    }

    @Override
    public ChatHistory saveUserMessage(Long userId, String message, List<Long> pictureIds, Long sessionId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType("user");
        // 保留第一张图片作为主图片（向后兼容）
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistory.setPictureId(pictureIds.get(0));
        }
        chatHistory.setSessionId(sessionId != null ? sessionId : IdWorker.getId());
        this.save(chatHistory);

        // 保存图片关联到关联表
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistoryPictureService.saveInputPictures(chatHistory.getId(), pictureIds);
        }
        return chatHistory;
    }

    @Override
    public ChatHistory saveUserMessage(Long userId, String message, List<Long> pictureIds, Long sessionId, Long spaceId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType("user");
        chatHistory.setSpaceId(spaceId);
        // 保留第一张图片作为主图片（向后兼容）
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistory.setPictureId(pictureIds.get(0));
        }
        chatHistory.setSessionId(sessionId != null ? sessionId : IdWorker.getId());
        this.save(chatHistory);

        // 保存图片关联到关联表
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistoryPictureService.saveInputPictures(chatHistory.getId(), pictureIds);
        }
        return chatHistory;
    }

    @Override
    public ChatHistory saveAiMessage(Long userId, String message, Long pictureId, Long sessionId) {
        // 向后兼容：单图片转为列表
        List<Long> pictureIds = pictureId != null ? Collections.singletonList(pictureId) : null;
        return saveAiMessage(userId, message, pictureIds, sessionId);
    }

    @Override
    public ChatHistory saveAiMessage(Long userId, String message, List<Long> pictureIds, Long sessionId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType("ai");
        // 保留第一张图片作为主图片（向后兼容）
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistory.setPictureId(pictureIds.get(0));
        }
        chatHistory.setSessionId(sessionId != null ? sessionId : IdWorker.getId());
        this.save(chatHistory);

        // 保存图片关联到关联表
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistoryPictureService.saveOutputPictures(chatHistory.getId(), pictureIds);
        }
        return chatHistory;
    }

    @Override
    public ChatHistory saveAiMessage(Long userId, String message, List<Long> pictureIds, Long sessionId, Long spaceId) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(userId);
        chatHistory.setMessage(message);
        chatHistory.setMessageType("ai");
        chatHistory.setSpaceId(spaceId);
        // 保留第一张图片作为主图片（向后兼容）
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistory.setPictureId(pictureIds.get(0));
        }
        chatHistory.setSessionId(sessionId != null ? sessionId : IdWorker.getId());
        this.save(chatHistory);

        // 保存图片关联到关联表
        if (!CollectionUtils.isEmpty(pictureIds)) {
            chatHistoryPictureService.saveOutputPictures(chatHistory.getId(), pictureIds);
        }
        return chatHistory;
    }

    // ==================== 新增管理功能实现 ====================

    @Override
    public Page<ChatHistorySessionVO> listSessionSummary(ChatHistorySessionQueryRequest queryRequest,
                                                         boolean includeUserId) {
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();


        Page<ChatHistorySessionVO> page = new Page<>(current, pageSize);

        Page<ChatHistorySessionVO> result = baseMapper.listSessionSummary(
                page,
                queryRequest.getSessionId(),
                queryRequest.getUserId(),
                queryRequest.getSpaceId(),
                queryRequest.getStartTime(),
                queryRequest.getEndTime(),
                !includeUserId);


        // 如果不需要返回userId，则清空
        if (!includeUserId && result.getRecords() != null) {
            result.getRecords().forEach(vo -> vo.setUserId(null));


        }


        return result;
    }

    /**
     * Lists paginated chat history with associated pictures
     */
    @Override
    public Page<ChatHistoryDetailVO> listSessionDetail(ChatHistoryDetailQueryRequest queryRequest,
                                                       HttpServletRequest request) {
        long current = queryRequest.getCurrent();
        long pageSize = queryRequest.getPageSize();

        // 限制分页大小
        if (pageSize > 100) {
            pageSize = 100;
        }

        // Step 1: 查询 ChatHistory 列表
        LambdaQueryWrapper<ChatHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatHistory::getSessionId, queryRequest.getSessionId());

        // 消息类型过滤
        if (queryRequest.getMessageType() != null && !queryRequest.getMessageType().isEmpty()) {
            wrapper.eq(ChatHistory::getMessageType, queryRequest.getMessageType());
        }

        // 时间范围过滤
        if (queryRequest.getStartTime() != null) {
            wrapper.ge(ChatHistory::getCreateTime, queryRequest.getStartTime());
        }
        if (queryRequest.getEndTime() != null) {
            wrapper.le(ChatHistory::getCreateTime, queryRequest.getEndTime());
        }

        // 排序
        String sortField = queryRequest.getSortField();
        String sortOrder = queryRequest.getSortOrder();
        // Applies dynamic ordering or defaults to creation time
        if (sortField != null && !sortField.isEmpty()) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            if ("createTime".equals(sortField)) {
                wrapper.orderBy(true, isAsc, ChatHistory::getCreateTime);
            } else if ("id".equals(sortField)) {
                wrapper.orderBy(true, isAsc, ChatHistory::getId);
            } else {
                // 默认按创建时间升序
                wrapper.orderByAsc(ChatHistory::getCreateTime);
            }
        } else {
            wrapper.orderByAsc(ChatHistory::getCreateTime);
        }

        Page<ChatHistory> historyPage = this.page(new Page<>(current, pageSize), wrapper);

        // Step 2: 批量查询关联的 ChatHistoryPicture
        List<ChatHistory> historyList = historyPage.getRecords();
        Map<Long, List<ChatHistoryPicture>> pictureMap = new HashMap<>();

        // Fetches and groups associated pictures by history ID
        List<ChatHistoryPicture> allPictures = null;
        // Fetches and groups associated pictures by history ID
        if (!CollectionUtils.isEmpty(historyList)) {
            List<Long> historyIds = historyList.stream()
                    .map(ChatHistory::getId)
                    .collect(Collectors.toList());

            allPictures = chatHistoryPictureService.list(
                    new LambdaQueryWrapper<ChatHistoryPicture>()
                            .in(ChatHistoryPicture::getChatHistoryId, historyIds)
                            .orderByAsc(ChatHistoryPicture::getSortOrder));

            // 按 chatHistoryId 分组
            pictureMap = allPictures.stream()
                    .collect(Collectors.groupingBy(ChatHistoryPicture::getChatHistoryId));
        }

        // Step 3: 批量查询 Picture 并转换为 PictureVO
        Map<Long, PictureVO> pictureVOMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(allPictures)) {
            // 提取所有不重复的 pictureId
            List<Long> pictureIds = allPictures.stream()
                    .map(ChatHistoryPicture::getPictureId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询 Picture 实体
            List<Picture> pictures = pictureService.listByIds(pictureIds);

            // 转换为 PictureVO 并建立 Map（pictureId -> PictureVO）
            for (Picture picture : pictures) {
                PictureVO pictureVO = pictureService.getPictureVO(picture, request);
                pictureVOMap.put(picture.getId(), pictureVO);
            }
        }

        // Step 4: 组装 VO，将 ChatHistoryPicture 转换为 ChatHistoryPictureVO
        Map<Long, List<ChatHistoryPicture>> finalPictureMap = pictureMap;
        Map<Long, PictureVO> finalPictureVOMap = pictureVOMap;

        // Maps histories to detail VOs with pictures
        List<ChatHistoryDetailVO> voList = historyList.stream().map(history -> {
            ChatHistoryDetailVO vo = new ChatHistoryDetailVO();
            BeanUtils.copyProperties(history, vo);

            // 将 ChatHistoryPicture 列表转换为 ChatHistoryPictureVO 列表，保持 sortOrder
            List<ChatHistoryPicture> chatPictures = finalPictureMap.getOrDefault(history.getId(), Collections.emptyList());
            // Maps pictures to detail VOs, preserving sort order
            List<ChatHistoryPictureVO> pictureVOs = chatPictures.stream()
                    .map(cp -> {
                        PictureVO pictureVO = finalPictureVOMap.get(cp.getPictureId());
                        if (pictureVO == null) {
                            return null; // 图片已被删除
                        }
                        // 组装 ChatHistoryPictureVO
                        ChatHistoryPictureVO chatPictureVO = new ChatHistoryPictureVO();
                        chatPictureVO.setPicture(pictureVO);
                        chatPictureVO.setPictureType(cp.getPictureType());
                        chatPictureVO.setSortOrder(cp.getSortOrder());
                        return chatPictureVO;
                    })
                    .filter(Objects::nonNull) // 过滤掉已删除的图片
                    .collect(Collectors.toList());

            vo.setPictures(pictureVOs);
            return vo;
        }).collect(Collectors.toList());

        // 构造返回结果
        Page<ChatHistoryDetailVO> resultPage = new Page<>(current, pageSize, historyPage.getTotal());
        resultPage.setRecords(voList);

        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBySessionId(Long sessionId) {
        // 1. 查询该 session 的所有 ChatHistory ID
        List<Long> historyIds = this.list(
                        new LambdaQueryWrapper<ChatHistory>()
                                .eq(ChatHistory::getSessionId, sessionId)
                                .select(ChatHistory::getId))
                .stream().map(ChatHistory::getId).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(historyIds)) {
            return true; // 没有记录，视为删除成功
        }

        // 2. 逻辑删除 ChatHistoryPicture（MyBatis-Plus 自动处理）
        chatHistoryPictureService.remove(
                new LambdaQueryWrapper<ChatHistoryPicture>()
                        .in(ChatHistoryPicture::getChatHistoryId, historyIds));

        // 3. 逻辑删除 ChatHistory
        return this.removeByIds(historyIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByIdWithCascade(Long id) {
        // 1. 逻辑删除关联的 ChatHistoryPicture
        chatHistoryPictureService.remove(
                new LambdaQueryWrapper<ChatHistoryPicture>()
                        .eq(ChatHistoryPicture::getChatHistoryId, id));

        // 2. 逻辑删除 ChatHistory
        return this.removeById(id);
    }

    @Override
    public boolean isSessionOwnedByUser(Long sessionId, Long userId) {
        if (sessionId == null || userId == null) {
            return false;
        }

        // 查询该 session 是否存在属于该用户的记录
        return this.count(
                new LambdaQueryWrapper<ChatHistory>()
                        .eq(ChatHistory::getSessionId, sessionId)
                        .eq(ChatHistory::getUserId, userId)) > 0;
    }

    @Override
    public boolean isRecordOwnedByUser(Long id, Long userId) {
        if (id == null || userId == null) {
            return false;
        }

        ChatHistory chatHistory = this.getById(id);
        return chatHistory != null && userId.equals(chatHistory.getUserId());
    }
}
