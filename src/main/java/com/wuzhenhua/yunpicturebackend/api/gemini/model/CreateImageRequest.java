package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import com.wuzhenhua.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class CreateImageRequest {
    String prompt;
    MultipartFile file;



}
