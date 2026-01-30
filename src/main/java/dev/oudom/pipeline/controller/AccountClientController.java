package dev.oudom.pipeline.controller;

import dev.oudom.pipeline.client.AccountClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/client/account")
@RequiredArgsConstructor
public class AccountClientController {

    private final AccountClient accountClient;

    @GetMapping("/secured")
    @CircuitBreaker(name = "account", fallbackMethod = "getSecuredDataFallback")
    public Map<String, Object> getSecuredData() {
        log.info("Calling account service...");
        return accountClient.getSecuredData();
    }

    // Fallback method - must have same return type and parameters + Exception
    private Map<String, Object> getSecuredDataFallback(Exception ex) {
        log.error("Account service is unavailable. Reason: {}", ex.getMessage());
        return Map.of(
                "status", "SERVICE_UNAVAILABLE",
                "message", "Account service is temporarily down. Please try again later.",
                "error", ex.getMessage()
        );
    }
}