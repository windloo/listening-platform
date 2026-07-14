package com.windloo.common.redis;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisUtilTest {
    @Test void set_delegates_with_ttl() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        new RedisUtil(redis).set("k", "v", 60);
        verify(ops).set("k", "v", Duration.ofSeconds(60));
    }
    @Test void get_delegates() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(ops);
        when(ops.get("k")).thenReturn("v");
        assertEquals("v", new RedisUtil(redis).get("k"));
    }
}