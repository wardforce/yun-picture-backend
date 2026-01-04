package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import lombok.Data;

@Data
public class ImageResponse {
    String text;
    PictureVO generatePicture;
    PictureVO uploadPicture;
}
