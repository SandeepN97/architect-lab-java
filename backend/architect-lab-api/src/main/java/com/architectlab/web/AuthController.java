package com.architectlab.web;

import com.architectlab.security.DemoUserService;
import com.architectlab.security.JwtService;
import com.architectlab.security.LoginRequest;
import com.architectlab.security.LoginResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final DemoUserService demoUserService;
    private final JwtService jwtService;

    public AuthController(DemoUserService demoUserService, JwtService jwtService) {
        this.demoUserService = demoUserService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return demoUserService.authenticate(request.username(), request.password())
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(new LoginResponse(jwtService.createToken(user), user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid demo credentials")));
    }

    @GetMapping("/demo-users")
    public Map<String, Object> demoUsers() {
        return Map.of("users", demoUserService.demoUsers());
    }
}
