package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

@Data
public class CreateChatRequest implements Serializable {
    String prompt;
    Long pictureId;

    /**
     * 对话 ID（可选，用于继续之前的对话）
     */
    Long sessionId;
}
