package com.wuzhenhua.yunpicturebackend.model.dto.picture;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "PictureUpdataRequest", description = "图片更新请求DTO")
public class PictureUpdateRequest implements Serializable {


    @Schema(description = "图片唯一标识符", example = "123", requiredMode = Schema.RequiredMode.REQUIRED, type = "long")
    private Long id;

 
   
    @Schema(description = "图片名称", example = "美丽风景.jpg")
    private String name;

    
    @Schema(description = "图片描述", example = "这是一张美丽的风景照片")
    private String introduction;

  
    @Schema(description = "图片分类", example = "风景")
    private String category;

    @Schema(description = "图片标签列表", example = "[\"自然\",\"风景\",\"山川\"]")
    private List<String> tags;

    @Schema(description = "图片主色调", example = "0xFFFFFF")
    private String picColor;

    private static final long serialVersionUID = 1L;
}
