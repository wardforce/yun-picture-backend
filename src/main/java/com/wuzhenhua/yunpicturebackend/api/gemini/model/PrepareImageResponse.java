package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import com.wuzhenhua.yunpicturebackend.model.entity.ChatHistory;
import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 第一阶段响应：准备图片
 * 用户上传图片后立即返回
 */
@Data
@Schema(description = "第一阶段响应：准备图片")
public class PrepareImageResponse implements Serializable {
    /**
     * 用户消息记录
     */
    private ChatHistory chatHistory;

    /**
     * 上传的图片
     */
    private PictureVO pictureVO;
}
