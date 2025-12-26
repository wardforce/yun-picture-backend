package com.wuzhenhua.yunpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 图片
 * @TableName picture
 */
@TableName(value ="picture")
@Data
@Schema(description = "图片实体类")
public class Picture {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "图片 id")
    private Long id;

    /**
     * 图片 url
     */
    @Schema(description = "图片 url", example = "http://127.0.0.1:9000/yun-picture-bucket/xxx.jpg")
    private String url;
    @Schema(description = "缩略图url")
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    @Schema(description = "图片名称", example = "美丽风景.jpg")
    private String name;

    /**
     * 简介
     */
    @Schema(description = "图片简介", example = "这是一张美丽的风景照片")
    private String introduction;

    /**
     * 分类
     */
    @Schema(description = "图片分类", example = "风景")
    private String category;


    /**
     * 标签（JSON 数组）
     */
    @Schema(description = "图片标签(JSON 数组)", example = "[\"自然\",\"风景\",\"山川\"]")
    private String tags;

    /**
     * 图片体积
     */
    @Schema(description = "图片体积(字节)", example = "1024000")
    private Long picSize;

    /**
     * 图片宽度
     */
    @Schema(description = "图片宽度(像素)", example = "1920")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @Schema(description = "图片高度(像素)", example = "1080")
    private Integer picHeight;

    /**
     * 图片宽高比例
     */
    @Schema(description = "图片宽高比例", example = "1.78")
    private Double picScale;

    /**
     * 图片格式
     */
    @Schema(description = "图片格式", example = "jpg")
    private String picFormat;

    /**
     * 创建用户 id
     */
    @Schema(description = "创建用户 id")
    private Long userId;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    private Date editTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @Schema(description = "是否删除(0-未删除, 1-已删除)")
    private Integer isDelete;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    @Schema(description = "审核状态(0-待审核,1-通过,2-拒绝)", example = "0")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @Schema(description = "审核信息", example = "图片内容不合规")
    private String reviewMessage;

    /**
     * 审核人 ID
     */
    @Schema(description = "审核人 id")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @Schema(description = "审核时间")
    private Date reviewTime;
    @Schema(description = "空间 id")
    private Long spaceId;
}