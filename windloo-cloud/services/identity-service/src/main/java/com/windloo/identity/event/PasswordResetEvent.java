package com.windloo.identity.event;
public record PasswordResetEvent(Long userId, String phone, String password) {}