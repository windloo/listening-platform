package com.windloo.common.redis;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;

@Component
public class RedisUtil {
    private final StringRedisTemplate redis;
    public RedisUtil(StringRedisTemplate redis) { this.redis = redis; }

    public void set(String key, String value, long ttlSeconds) {
        redis.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
    }
    public String get(String key) { return redis.opsForValue().get(key); }
    public boolean delete(String key) { return Boolean.TRUE.equals(redis.delete(key)); }

    public Long incr(String key) { return redis.opsForValue().increment(key); }
    public void expire(String key, long ttlSeconds) { redis.expire(key, Duration.ofSeconds(ttlSeconds)); }
}