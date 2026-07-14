package com.windloo.identity.sms;
public interface SmsSender {
    void sendAsync(String phone, String content);
}