package com.roomify.core.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscountServiceTest {
    @Test
    void applyDiscount_noRules_returnsSame() {
        DiscountService ds = new DiscountService();
        double out = ds.applyDiscount("u1", 200.0);
        assertEquals(200.0, out);
    }
}
