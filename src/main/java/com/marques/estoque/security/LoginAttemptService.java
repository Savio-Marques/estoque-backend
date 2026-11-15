package com.marques.estoque.security;

import com.marques.estoque.exception.TooManyRequestsException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_SECONDS = 15 * 60; // 15 minutos

    private final Map<String, AttemptRecord> attempts = new ConcurrentHashMap<>();

    public void checkBlocked(String key) {
        AttemptRecord record = attempts.get(key);
        if (record == null) {
            return;
        }
        Instant now = Instant.now();
        if (record.blockedUntil() != null) {
            if (now.isBefore(record.blockedUntil())) {
                throw new TooManyRequestsException(
                        "Muitas tentativas de login inválidas. Tente novamente em 15 minutos."
                );
            }
            attempts.remove(key);
        }
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (k, current) -> {
            if (current == null || now.isAfter(current.windowExpiresAt())) {
                return new AttemptRecord(1, now.plusSeconds(BLOCK_DURATION_SECONDS), null);
            }
            int nextCount = current.count() + 1;
            Instant blockedUntil = nextCount >= MAX_ATTEMPTS
                    ? now.plusSeconds(BLOCK_DURATION_SECONDS)
                    : null;
            return new AttemptRecord(nextCount, current.windowExpiresAt(), blockedUntil);
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private record AttemptRecord(int count, Instant windowExpiresAt, Instant blockedUntil) {
    }
}
