package ch.nsource.tokengenerator.service;
 
import ch.nsource.tokengenerator.model.CodeEntry;
import ch.nsource.tokengenerator.model.OperatingSystem;
import ch.nsource.tokengenerator.repository.CodeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class CodeService {
    private static final Duration MAX_TTL = Duration.ofHours(2);
    private static final int MAX_RETRIES = 10;
    private final CodeRepository repository;
    private final SecureRandom random = new SecureRandom();

    public CodeService(CodeRepository repository) {
        this.repository = repository;
    }

    public CodeEntry generateAndStore(OperatingSystem serverOs) {
        Instant now = Instant.now();
        Instant expires = now.plus(MAX_TTL);

        int attempts = 0;
        while (true) {
            attempts++;
            String code = sixDigitCode();
            CodeEntry entry = new CodeEntry(null, code, now, expires, serverOs);
            try {
                return repository.insert(entry);
            } catch (DataIntegrityViolationException dup) {
                if (attempts >= MAX_RETRIES) {
                    throw dup;
                }
                // retry on unique violation
            }
        }
    }

    public Optional<CodeEntry> getIfValid(String code) {
        Optional<CodeEntry> opt = repository.findByCode(code);
        if (opt.isEmpty()) return Optional.empty();
        CodeEntry entry = opt.get();
        if (entry.getExpiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(entry);
    }

    public java.util.List<CodeEntry> getAllCodes() {
        return repository.findAll();
    }

    public boolean deleteCode(String code) {
        return repository.deleteByCode(code);
    }

    private String sixDigitCode() {
        int n = random.nextInt(1_000_000); // 0..999999
        return String.format("%06d", n);
    }
}
