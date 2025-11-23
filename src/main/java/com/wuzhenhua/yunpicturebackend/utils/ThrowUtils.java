package com.wuzhenhua.yunpicturebackend.utils;

import com.wuzhenhua.yunpicturebackend.exception.BusinessException;
import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;

/**
 * 异常处理工具类的静态方法，用于根据条件抛出不同类型的异常。
 */
public class ThrowUtils {

    /**
     * 如果条件为 true，则抛出指定的 RuntimeException。
     *
     * @param condition 判断条件，当为 true 时抛出异常
     * @param e         要抛出的 RuntimeException 实例
     */
    public static void throwIf(boolean condition, RuntimeException e) {
        if (condition) {
            throw e;
        }
    }

    /**
     * 如果条件为 true，则抛出带有指定 ErrorCode 消息的 RuntimeException。
     *
     * @param condition 判断条件，当为 true 时抛出异常
     * @param errorCode 包含异常消息的 ErrorCode 枚举实例
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 如果条件为 true，则抛出带有指定 ErrorCode 消息的 RuntimeException。
     *
     * @param condition 判断条件，当为 true 时抛出异常
     * @param errorCode 包含异常消息的 ErrorCode 枚举实例
     * @param message   异常消息内容
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
