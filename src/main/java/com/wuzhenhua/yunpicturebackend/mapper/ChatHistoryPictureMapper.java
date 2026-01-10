package com.wuzhenhua.yunpicturebackend.mapper;

import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistoryPicture;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * @author ward
 * @description 针对表【chat_history_picture(对话历史图片关联表)】的数据库操作Mapper
 * @Entity com.wuzhenhua.yunpicturebackend.model.entity.ChatHistoryPicture
 */
public interface ChatHistoryPictureMapper extends BaseMapper<ChatHistoryPicture> {

    /**
     * 根据对话历史ID查询关联图片
     */
    List<ChatHistoryPicture> selectByChatHistoryId(Long chatHistoryId);

    /**
     * 批量插入图片关联
     */
    int batchInsert(List<ChatHistoryPicture> list);
}
