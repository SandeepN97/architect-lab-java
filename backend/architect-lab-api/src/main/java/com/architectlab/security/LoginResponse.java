package com.architectlab.security;

public record LoginResponse(String accessToken, UserProfile user) {
}
