package com.wuzhenhua.yunpicturebackend.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NumberCastUtilsTest {

    @Test
    void toLongValueShouldSupportCommonNumberTypes() {
        assertEquals(12L, NumberCastUtils.toLongValue(12L));
        assertEquals(34L, NumberCastUtils.toLongValue(34));
        assertEquals(56L, NumberCastUtils.toLongValue(new BigDecimal("56")));
        assertEquals(0L, NumberCastUtils.toLongValue(null));
    }

    @Test
    void toLongValueShouldRejectNonNumberValues() {
        assertThrows(IllegalArgumentException.class, () -> NumberCastUtils.toLongValue("123"));
    }
}
