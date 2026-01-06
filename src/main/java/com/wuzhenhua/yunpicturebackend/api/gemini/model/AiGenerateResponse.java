package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import lombok.Data;

import java.io.Serializable;

/**
 * 第二阶段响应：AI 生成图片
 * Gemini 生成完成后返回
 */
@Data
public class AiGenerateResponse implements Serializable {
    /**
     * AI 响应消息记录
     */
    private ChatHistory chatHistory;

    /**
     * 生成的图片
     */
    private PictureVO pictureVO;
}
