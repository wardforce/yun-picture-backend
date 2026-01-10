package com.wuzhenhua.yunpicturebackend.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 聊天历史关联图片视图对象
 */
@Data
@Schema(name = "ChatHistoryPictureVO", description = "聊天历史关联图片视图对象")
public class ChatHistoryPictureVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 完整图片信息
     */
    @Schema(description = "图片详细信息")
    private PictureVO picture;

    /**
     * 图片类型：INPUT（用户上传）/ OUTPUT（AI生成）
     */
    @Schema(description = "图片类型：INPUT/OUTPUT")
    private String pictureType;

    /**
     * 排序顺序
     */
    @Schema(description = "显示顺序")
    private Integer sortOrder;
}
