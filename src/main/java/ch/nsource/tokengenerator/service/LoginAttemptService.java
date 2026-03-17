package ch.nsource.tokengenerator.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final int maxAttempts;
    private final long lockoutSeconds;

    private record AttemptState(int count, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(
            @Value("${admin.login.max-attempts:5}") int maxAttempts,
            @Value("${admin.login.lockout-minutes:15}") long lockoutMinutes) {
        this.maxAttempts = maxAttempts;
        this.lockoutSeconds = lockoutMinutes * 60;
    }

    public boolean isBlocked(String ip) {
        AttemptState state = attempts.get(ip);
        if (state == null || state.lockedUntil() == null) return false;
        if (Instant.now().isBefore(state.lockedUntil())) return true;
        attempts.remove(ip); // lockout expired
        return false;
    }

    public void recordFailure(String ip) {
        attempts.merge(ip, new AttemptState(1, null), (existing, ignored) -> {
            int count = existing.count() + 1;
            Instant lock = count >= maxAttempts ? Instant.now().plusSeconds(lockoutSeconds) : null;
            return new AttemptState(count, lock);
        });
    }

    public void recordSuccess(String ip) {
        attempts.remove(ip);
    }
}
