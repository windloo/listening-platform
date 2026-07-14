package com.windloo.common.api;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonResponseTest {
    @Test void ok_sets_zero_code_and_data() {
        JsonResponse<String> r = JsonResponse.ok("hi");
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMsg());
        assertEquals("hi", r.getData());
    }
    @Test void fail_sets_code_and_null_data() {
        JsonResponse<String> r = JsonResponse.fail(40001, "登录失败");
        assertEquals(40001, r.getCode());
        assertNull(r.getData());
    }
}