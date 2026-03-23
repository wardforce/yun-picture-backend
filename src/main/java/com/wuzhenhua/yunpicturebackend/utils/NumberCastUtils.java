package com.wuzhenhua.yunpicturebackend.utils;

public final class NumberCastUtils {

    private NumberCastUtils() {
    }

    public static long toLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("统计结果不是数值类型: " + value.getClass().getName());
    }
}
