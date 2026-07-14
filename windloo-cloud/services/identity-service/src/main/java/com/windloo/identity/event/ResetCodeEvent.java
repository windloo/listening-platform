package com.windloo.identity.event;
public record ResetCodeEvent(String phone, String code) {}