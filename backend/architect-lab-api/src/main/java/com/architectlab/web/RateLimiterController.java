package com.architectlab.web;

import com.architectlab.command.SimulationState;
import com.architectlab.rate.RateLimitAlgorithm;
import com.architectlab.rate.RateLimitConfig;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rate-limiter")
public class RateLimiterController {
    private final SimulationState simulationState;

    public RateLimiterController(SimulationState simulationState) {
        this.simulationState = simulationState;
    }

    @GetMapping("/config")
    public RateLimitConfig config() {
        return simulationState.rateLimitConfig();
    }

    @PostMapping("/config")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public RateLimitConfig configure(@RequestBody Map<String, Object> request, Principal principal) {
        RateLimitAlgorithm algorithm = parseAlgorithm(request.getOrDefault("algorithm", "TOKEN_BUCKET"));
        int limit = parsePositiveInt(request.get("limit"), 100, "limit");
        int windowSeconds = parsePositiveInt(request.get("windowSeconds"), 60, "windowSeconds");
        simulationState.configureRateLimiter(algorithm, limit, windowSeconds);
        return simulationState.rateLimitConfig();
    }

    private RateLimitAlgorithm parseAlgorithm(Object value) {
        try {
            return RateLimitAlgorithm.valueOf(String.valueOf(value).trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new InvalidRateLimiterConfigException("Unsupported rate-limit algorithm: " + value);
        }
    }

    private int parsePositiveInt(Object value, int fallback, String fieldName) {
        int parsed;
        if (value == null) {
            parsed = fallback;
        } else if (value instanceof Number number) {
            parsed = number.intValue();
        } else {
            try {
                parsed = Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException exception) {
                throw new InvalidRateLimiterConfigException(fieldName + " must be a positive integer");
            }
        }
        if (parsed <= 0) {
            throw new InvalidRateLimiterConfigException(fieldName + " must be greater than zero");
        }
        return parsed;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static class InvalidRateLimiterConfigException extends RuntimeException {
        private InvalidRateLimiterConfigException(String message) {
            super(message);
        }
    }
}
