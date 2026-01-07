package ch.nsource.tokengenerator.web;

import ch.nsource.tokengenerator.model.CodeEntry;
import ch.nsource.tokengenerator.model.OperatingSystem;
import ch.nsource.tokengenerator.service.CodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/codes")
public class CodeController {
    private final CodeService codeService;

    public CodeController(CodeService codeService) {
        this.codeService = codeService;
    }

    private Map<String, Object> entryToMap(CodeEntry entry) {
        return Map.of(
                "code", entry.getCode(),
                "expiresAt", entry.getExpiresAt().toString(),
                "createdAt", entry.getCreatedAt().toString(),
                "serverOs", entry.getServerOs()
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCode(
            @RequestBody Map<String, String> request) {
        String osStr = request.getOrDefault("serverOs", "MACOS");
        OperatingSystem os;
        try {
            os = OperatingSystem.valueOf(osStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid serverOs. Must be MACOS or WINDOWS",
                    "timestamp", Instant.now().toString()
            ));
        }
        CodeEntry entry = codeService.generateAndStore(os);
        return ResponseEntity.status(HttpStatus.CREATED).body(entryToMap(entry));
    }

    @GetMapping
    public ResponseEntity<java.util.List<Map<String, Object>>> getAllCodes() {
        java.util.List<Map<String, Object>> codes = codeService.getAllCodes().stream()
                .map(this::entryToMap)
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
        return ResponseEntity.ok(entryToMap(entry));
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
