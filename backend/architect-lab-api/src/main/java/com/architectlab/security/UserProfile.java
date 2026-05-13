package com.architectlab.security;

import java.util.List;

public record UserProfile(String username, List<String> roles) {
}
