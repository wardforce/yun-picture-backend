package com.wuzhenhua.yunpicturebackend.common;

import com.wuzhenhua.yunpicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应类，用于封装接口返回的数据结构。
 *
 * @param <T> 响应数据的类型
 */
@Data
public class BaseResponse<T> implements Serializable {
    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 构造方法，初始化状态码、数据和消息。
     *
     * @param code    状态码
     * @param data    响应数据
     * @param message 响应消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 构造方法，初始化数据和状态码。
     *
     * @param data 响应数据
     * @param code 状态码
     */
    public BaseResponse(T data, int code) {
        this.data = data;
        this.code = code;
    }

    /**
     * 构造方法，初始化状态码。
     *
     * @param errorCode 状态码
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }


}