package com.windloo.listening.config;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager m = new CaffeineCacheManager(
            "categories", "category", "albums_by_cat", "album", "episodes_by_album", "episode");
        m.setCaffeine(Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).maximumSize(500));
        return m;
    }
}