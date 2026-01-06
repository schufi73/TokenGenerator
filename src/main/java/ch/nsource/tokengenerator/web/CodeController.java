package ch.nsource.tokengenerator.web;

import ch.nsource.tokengenerator.model.CodeEntry;
import ch.nsource.tokengenerator.service.CodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/codes")
public class CodeController {
    private final CodeService codeService;

    public CodeController(CodeService codeService) {
        this.codeService = codeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCode() {
        CodeEntry entry = codeService.generateAndStore();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "code", entry.getCode(),
                "expiresAt", entry.getExpiresAt().toString(),
                "createdAt", entry.getCreatedAt().toString()
        ));
    }

    @GetMapping
    public ResponseEntity<java.util.List<Map<String, Object>>> getAllCodes() {
        java.util.List<Map<String, Object>> codes = codeService.getAllCodes().stream()
                .map(entry -> Map.<String, Object>of(
                        "code", entry.getCode(),
                        "expiresAt", entry.getExpiresAt().toString(),
                        "createdAt", entry.getCreatedAt().toString()
                ))
                .toList();
        return ResponseEntity.ok(codes);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Map<String, Object>> getCode(@PathVariable String code) {
        Optional<CodeEntry> opt = codeService.getIfValid(code);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Code not found or expired",
                    "timestamp", Instant.now().toString()
            ));
        }
        CodeEntry entry = opt.get();
        return ResponseEntity.ok(Map.of(
                "code", entry.getCode(),
                "expiresAt", entry.getExpiresAt().toString(),
                "createdAt", entry.getCreatedAt().toString()
        ));
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCode(@PathVariable String code) {
        boolean deleted = codeService.deleteCode(code);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
