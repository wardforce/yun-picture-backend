package com.wuzhenhua.yunpicturebackend.utils;

import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;

public final class SpaceAnalyzeScopeUtils {

    private SpaceAnalyzeScopeUtils() {
    }

    public static boolean isGlobalScope(SpaceAnalyzeRequest request) {
        return request != null && (request.isQueryAll() || request.isQueryPublic());
    }
}
