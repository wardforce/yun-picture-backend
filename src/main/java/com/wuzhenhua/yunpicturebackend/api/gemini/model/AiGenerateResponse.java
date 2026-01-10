package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * AI 生成图片响应
 * Gemini 生成完成后返回
 */
@Data
public class AiGenerateResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI 响应消息记录
     */
    private ChatHistory chatHistory;

    /**
     * 生成的图片列表
     */
    private List<PictureVO> pictureVOs;

    /**
     * AI 响应文本
     */
    private String aiText;
}
