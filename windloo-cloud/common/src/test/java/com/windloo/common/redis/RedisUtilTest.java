package com.windloo.common.redis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisUtilTest {
    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> valueOps;
    @InjectMocks RedisUtil util;

    @Test void incr_delegates_to_valueOps_increment() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("k")).thenReturn(5L);
        assertEquals(5L, util.incr("k"));
    }

    @Test void expire_delegates_with_seconds() {
        when(redis.expire(eq("k"), any(Duration.class))).thenReturn(true);
        util.expire("k", 60);
        verify(redis).expire("k", Duration.ofSeconds(60));
    }
}