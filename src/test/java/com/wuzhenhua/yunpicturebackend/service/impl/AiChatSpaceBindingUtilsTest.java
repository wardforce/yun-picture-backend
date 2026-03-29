package com.wuzhenhua.yunpicturebackend.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AiChatSpaceBindingUtilsTest {

    @Test
    void buildUploadPathPrefixShouldUsePublicFolderForNullSpace() {
        assertEquals("public/7", AiChatSpaceBindingUtils.buildUploadPathPrefix(null, 7L));
    }

    @Test
    void buildUploadPathPrefixShouldUseSpaceFolderForOwnedSpace() {
        assertEquals("space/11/7", AiChatSpaceBindingUtils.buildUploadPathPrefix(11L, 7L));
    }

    @Test
    void resolveChatSpaceIdShouldUseSelectedSpaceForNewConversation() {
        assertEquals(9L, AiChatSpaceBindingUtils.resolveChatSpaceId(9L, null, false));
    }

    @Test
    void resolveChatSpaceIdShouldKeepPublicSpaceForExistingConversation() {
        assertNull(AiChatSpaceBindingUtils.resolveChatSpaceId(9L, null, true));
    }

    @Test
    void resolveChatSpaceIdShouldKeepExistingOwnedSpaceForExistingConversation() {
        assertEquals(12L, AiChatSpaceBindingUtils.resolveChatSpaceId(null, 12L, true));
    }
}
