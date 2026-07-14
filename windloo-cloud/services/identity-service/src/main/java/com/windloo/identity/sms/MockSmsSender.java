package com.windloo.identity.sms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsSender implements SmsSender {
    @Override
    public void sendAsync(String phone, String content) {
        log.info("[MOCK SMS] phone={}, content={}", phone, content);
    }
}