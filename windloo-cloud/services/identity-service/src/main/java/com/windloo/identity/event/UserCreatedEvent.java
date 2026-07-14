package com.windloo.identity.event;
public record UserCreatedEvent(Long userId, String phone, String password) {}