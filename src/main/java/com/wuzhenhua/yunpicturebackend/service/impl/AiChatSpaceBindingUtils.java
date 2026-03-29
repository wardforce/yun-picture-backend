package com.wuzhenhua.yunpicturebackend.service.impl;

public final class AiChatSpaceBindingUtils {

    private AiChatSpaceBindingUtils() {
    }

    public static String buildUploadPathPrefix(Long spaceId, Long userId) {
        return spaceId == null
                ? String.format("public/%s", userId)
                : String.format("space/%s/%s", spaceId, userId);
    }

    public static Long resolveChatSpaceId(Long requestSpaceId, Long existingSessionSpaceId, boolean hasExistingSession) {
        return hasExistingSession ? existingSessionSpaceId : requestSpaceId;
    }
}
