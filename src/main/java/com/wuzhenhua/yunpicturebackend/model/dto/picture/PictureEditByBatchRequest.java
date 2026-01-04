package com.wuzhenhua.yunpicturebackend.model.dto.picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "批量编辑图片请求参数")
public class PictureEditByBatchRequest implements Serializable {

    /**
     * 图片 id 列表
     */
    private List<Long> pictureIdList;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;
    /**
     * 命名规则
     */
    @Schema(description = "图片命名规则，支持变量：{index}、{category}、{tags}、{time}、{random}，例如：{index}_{category}_{tags}_{time}_{random}")
    private String nameRule;


    private static final long serialVersionUID = 1L;
}
