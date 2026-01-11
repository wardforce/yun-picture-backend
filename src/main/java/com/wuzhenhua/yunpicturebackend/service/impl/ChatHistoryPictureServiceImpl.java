package com.wuzhenhua.yunpicturebackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuzhenhua.yunpicturebackend.mapper.ChatHistoryPictureMapper;
import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistoryPicture;
import com.wuzhenhua.yunpicturebackend.service.ChatHistoryPictureService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ward
 * @description 针对表【chat_history_picture(对话历史图片关联表)】的数据库操作Service实现
 */
@Service
public class ChatHistoryPictureServiceImpl extends ServiceImpl<ChatHistoryPictureMapper, ChatHistoryPicture>
        implements ChatHistoryPictureService {

    @Override
    public void saveInputPictures(Long chatHistoryId, List<Long> pictureIds) {
        savePictures(chatHistoryId, pictureIds, "INPUT");
    }

    @Override
    public void saveOutputPictures(Long chatHistoryId, List<Long> pictureIds) {
        savePictures(chatHistoryId, pictureIds, "OUTPUT");
    }

    @Override
    public List<ChatHistoryPicture> getByChatHistoryId(Long chatHistoryId) {
        return baseMapper.selectByChatHistoryId(chatHistoryId);
    }

    @Override
    public boolean deleteByPictureId(Long pictureId) {
        return this.remove(new LambdaQueryWrapper<ChatHistoryPicture>()
                .eq(ChatHistoryPicture::getPictureId, pictureId));
    }

    private void savePictures(Long chatHistoryId, List<Long> pictureIds, String pictureType) {
        if (CollectionUtils.isEmpty(pictureIds)) {
            return;
        }
        List<ChatHistoryPicture> list = new ArrayList<>();
        for (int i = 0; i < pictureIds.size(); i++) {
            ChatHistoryPicture pic = new ChatHistoryPicture();
            pic.setChatHistoryId(chatHistoryId);
            pic.setPictureId(pictureIds.get(i));
            pic.setPictureType(pictureType);
            pic.setSortOrder(i);
            list.add(pic);
        }
        this.saveBatch(list);
    }
}
