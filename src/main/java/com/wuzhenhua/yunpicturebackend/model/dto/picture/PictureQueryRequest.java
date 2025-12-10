package com.wuzhenhua.yunpicturebackend.model.dto.picture;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.wuzhenhua.yunpicturebackend.common.PageRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)  
@Data  
@Schema(name = "PictureQueryRequest", description = "图片查询请求DTO")
public class PictureQueryRequest extends PageRequest implements Serializable {  
  
    /**  
     * id  
     */  
    @Schema(description = "图片 id", example = "123", requiredMode = Schema.RequiredMode.REQUIRED, type = "long")
    private Long id;  
  
    /**  
     * 图片名称  
     */  
    @Schema(description = "图片名称", example = "美丽风景.jpg")
    private String name;  
  
    /**  
     * 简介  
     */  
    @Schema(description = "图片描述", example = "这是一张美丽的风景照片")
    private String introduction;  
  
    /**  
     * 分类  
     */  
    @Schema(description = "图片分类", example = "风景")
    private String category;  
  
    /**  
     * 标签  
     */  
    @Schema(description = "图片标签列表", example = "[\"自然\",\"风景\",\"山川\"]")
    private List<String> tags;  
  
    /**  
     * 文件体积  
     */  
    @Schema(description = "图片文件体积", example = "1024000", type = "long")
    private Long picSize;  
  
    /**  
     * 图片宽度  
     */  
    @Schema(description = "图片宽度", example = "1920", type = "integer")
    private Integer picWidth;  
  
    /**  
     * 图片高度  
     */  
    @Schema(description = "图片高度", example = "1080", type = "integer")
    private Integer picHeight;  
  
    /**  
     * 图片比例  
     */  
    @Schema(description = "图片比例", example = "1.7777777777777777", type = "double")
    private Double picScale;  
  
    /**  
     * 图片格式  
     */  
    @Schema(description = "图片格式", example = "jpg", type = "string")
    private String picFormat;  
  
    /**  
     * 搜索词（同时搜名称、简介等）  
     */  
    @Schema(description = "搜索词（同时搜名称、简介等）", example = "风景")
    private String searchText;  
  
    /**  
     * 用户 id  
     */  
    private Long userId;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;
  
    private static final long serialVersionUID = 1L;  
}

