package ch.nsource.tokengenerator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService(5, 15);
    }

    @Test
    void testNotBlockedInitially() {
        assertFalse(service.isBlocked("1.2.3.4"));
    }

    @Test
    void testNotBlockedBeforeMaxAttempts() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure("1.2.3.4");
        }
        assertFalse(service.isBlocked("1.2.3.4"));
    }

    @Test
    void testBlockedAfterMaxAttempts() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure("1.2.3.4");
        }
        assertTrue(service.isBlocked("1.2.3.4"));
    }

    @Test
    void testSuccessResetsAttempts() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure("1.2.3.4");
        }
        service.recordSuccess("1.2.3.4");
        assertFalse(service.isBlocked("1.2.3.4"));

        // Counter reset: 4 more failures should not block
        for (int i = 0; i < 4; i++) {
            service.recordFailure("1.2.3.4");
        }
        assertFalse(service.isBlocked("1.2.3.4"));
    }

    @Test
    void testDifferentIpsTrackedIndependently() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure("1.2.3.4");
        }
        assertTrue(service.isBlocked("1.2.3.4"));
        assertFalse(service.isBlocked("5.6.7.8"));
    }
}
