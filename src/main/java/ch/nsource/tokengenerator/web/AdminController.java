package ch.nsource.tokengenerator.web;

import ch.nsource.tokengenerator.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final byte[] adminPasswordBytes;
    private final LoginAttemptService loginAttemptService;

    public AdminController(
            @Value("${admin.password}") String adminPassword,
            LoginAttemptService loginAttemptService) {
        this.adminPasswordBytes = adminPassword.getBytes();
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {

        String ip = resolveClientIp(httpRequest);

        if (loginAttemptService.isBlocked(ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "success", false,
                    "error", "Too many failed attempts. Try again later."
            ));
        }

        String password = request.get("password");
        if (password == null || !MessageDigest.isEqual(password.getBytes(), adminPasswordBytes)) {
            loginAttemptService.recordFailure(ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "error", "Invalid password"
            ));
        }

        loginAttemptService.recordSuccess(ip);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
