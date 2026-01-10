package com.wuzhenhua.yunpicturebackend.mapper;

import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.vo.ChatHistorySessionVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * @author ward
 * @description 针对表【chat_history(对话历史)】的数据库操作Mapper
 * @createDate 2026-01-05 16:09:41
 * @Entity com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory
 */
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /**
     * 分页查询会话摘要列表
     *
     * @param page          分页对象
     * @param sessionId     会话ID（可选）
     * @param userId        用户ID（可选）
     * @param spaceId       空间ID（可选）
     * @param startTime     开始时间（可选）
     * @param endTime       结束时间（可选）
     * @param filterDeleted 是否过滤已删除会话（用户接口为true，管理员接口为false）
     * @return 会话摘要分页结果
     */
    Page<ChatHistorySessionVO> listSessionSummary(
            @Param("page") Page<ChatHistorySessionVO> page,
            @Param("sessionId") Long sessionId,
            @Param("userId") Long userId,
            @Param("spaceId") Long spaceId,
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("filterDeleted") boolean filterDeleted);
}
