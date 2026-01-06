package com.wuzhenhua.yunpicturebackend.api.gemini.model;

import com.wuzhenhua.yunpicturebackend.model.vo.PictureVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class ImageResponse implements Serializable {
    String text;
    PictureVO generatePicture;
    PictureVO uploadPicture;
}
