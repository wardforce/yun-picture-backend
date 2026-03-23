package com.wuzhenhua.yunpicturebackend.service;

import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistoryPicture;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author ward
 *  针对表【chat_history_picture(对话历史图片关联表)】的数据库操作Service
 */
public interface ChatHistoryPictureService extends IService<ChatHistoryPicture> {

    /**
     * 保存输入图片关联
     *
     * @param chatHistoryId 对话历史ID
     * @param pictureIds    图片ID列表
     */
    void saveInputPictures(Long chatHistoryId, List<Long> pictureIds);

    /**
     * 保存输出图片关联
     *
     * @param chatHistoryId 对话历史ID
     * @param pictureIds    图片ID列表
     */
    void saveOutputPictures(Long chatHistoryId, List<Long> pictureIds);

    /**
     * 根据对话历史ID查询关联图片
     *
     * @param chatHistoryId 对话历史ID
     * @return 图片关联列表
     */
    List<ChatHistoryPicture> getByChatHistoryId(Long chatHistoryId);

    /**
     * 根据图片ID删除关联
     * @param pictureId
     * @return
     */
    boolean deleteByPictureId(Long pictureId);


}
