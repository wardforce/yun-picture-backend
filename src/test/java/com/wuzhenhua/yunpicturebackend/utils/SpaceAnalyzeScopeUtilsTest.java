package com.wuzhenhua.yunpicturebackend.utils;

import com.wuzhenhua.yunpicturebackend.model.dto.space.analyze.SpaceAnalyzeRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpaceAnalyzeScopeUtilsTest {

    @Test
    void isGlobalScopeShouldReturnTrueWhenQueryAllIsEnabled() {
        SpaceAnalyzeRequest request = new SpaceAnalyzeRequest();
        request.setQueryAll(true);

        assertTrue(SpaceAnalyzeScopeUtils.isGlobalScope(request));
    }

    @Test
    void isGlobalScopeShouldReturnTrueWhenQueryPublicIsEnabled() {
        SpaceAnalyzeRequest request = new SpaceAnalyzeRequest();
        request.setQueryPublic(true);

        assertTrue(SpaceAnalyzeScopeUtils.isGlobalScope(request));
    }

    @Test
    void isGlobalScopeShouldReturnFalseForSpecificSpaceRequests() {
        SpaceAnalyzeRequest request = new SpaceAnalyzeRequest();
        request.setSpaceId(1L);

        assertFalse(SpaceAnalyzeScopeUtils.isGlobalScope(request));
    }
}
