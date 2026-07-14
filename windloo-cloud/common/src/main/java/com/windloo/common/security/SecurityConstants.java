package com.windloo.common.security;

public final class SecurityConstants {
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLES = "roles";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    private SecurityConstants() {}
}