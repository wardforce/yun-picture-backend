package com.wuzhenhua.yunpicturebackend.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;

@lombok.Data
@Schema(description = "批量上传图片请求参数")
public class PictureUploadByBatchRequest implements java.io.Serializable{
    @Schema(description = "搜索文本")
    private String searchText;
    @Schema(description = "图片数量")
    private Integer count=10;
    @Schema(description = "图片名称前缀")
    private String namePrefix;
    private static final long serialVersionUID = 1L;
}
