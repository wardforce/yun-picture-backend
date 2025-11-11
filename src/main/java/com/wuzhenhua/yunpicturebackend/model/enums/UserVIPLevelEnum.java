package com.wuzhenhua.yunpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum UserVIPLevelEnum {
    STANDARD("标准", "standard"),
    PRO("专业版", "pro"),
    MAX("企业版", "max");
    private final String text;
    private final String value;

    UserVIPLevelEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static UserVIPLevelEnum getUserVIPLevelEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (UserVIPLevelEnum anEnum : UserVIPLevelEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
