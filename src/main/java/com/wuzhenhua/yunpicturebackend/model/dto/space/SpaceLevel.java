package com.wuzhenhua.yunpicturebackend.model.dto.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Space level configuration for user spaces")
public class SpaceLevel {

    private int value;

    private String text;

    private long maxCount;

    private long maxSize;
}
