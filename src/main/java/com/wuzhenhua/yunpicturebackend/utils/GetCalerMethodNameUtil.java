package com.wuzhenhua.yunpicturebackend.utils;

public class GetCalerMethodNameUtil {
    /**
     * 通用方法:获取调用者信息
     *
     * @param depth 调用深度(0=当前方法的调用者, 1=上一级调用者, 以此类推)
     * @return 调用方法的方法名
     */
    public String getCallerMethodName(int depth) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // stackTrace[0] 是 getStackTrace 方法
        // stackTrace[1] 是当前方法 getCallerMethodName
        // stackTrace[2] 是调用 getCallerMethodName 的方法
        // getStackTrace() 本身占用索引 0
        // 当前方法占用索引 1
        // 调用当前方法的方法从索引 2 开始
        int targetIndex = 2 + depth;

        if (stackTrace.length > targetIndex) {
            StackTraceElement caller = stackTrace[targetIndex];
            return caller.getMethodName();
        }
        return "Unknown";
    }
}
