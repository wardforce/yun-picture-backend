package com.wuzhenhua.yunpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Date;
import lombok.Data;

/**
 * 空间
 * @TableName space
 */
@TableName(value ="space")
@Data
@Schema(description = "空间实体类")
public class Space {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "空间 id")
    private Long id;

    /**
     * 空间名称
     */
    @Schema(description = "空间名称", example = "我的图片空间")
    private String spaceName;

    /**
     * 空间级别：0-普通版 1-专业版 2-旗舰版
     */
    @Schema(description = "空间级别(0-普通版,1-专业版,2-旗舰版)", example = "0")
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大小
     */
    @Schema(description = "空间图片的最大总大小(字节)", example = "10485760")
    private Long maxSize;

    /**
     * 空间图片的最大数量
     */
    @Schema(description = "空间图片的最大数量", example = "100")
    private Long maxCount;

    /**
     * 当前空间下图片的总大小
     */
    @Schema(description = "当前空间下图片的总大小(字节)", example = "5242880")
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    @Schema(description = "当前空间下的图片数量", example = "50")
    private Long totalCount;

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
    @Schema(description = "是否删除(0-未删除,1-已删除)")
    private Integer isDelete;
}