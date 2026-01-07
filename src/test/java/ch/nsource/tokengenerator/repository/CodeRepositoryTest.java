package ch.nsource.tokengenerator.repository;

import ch.nsource.tokengenerator.model.CodeEntry;
import ch.nsource.tokengenerator.model.OperatingSystem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import(CodeRepository.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CodeRepositoryTest {

    @Autowired
    private CodeRepository repository;

    @Test
    public void testInsertAndFindByCode() {
        CodeEntry entry = new CodeEntry(null, "123456", Instant.now(), Instant.now().plusSeconds(3600), OperatingSystem.WINDOWS);
        repository.insert(entry);

        Optional<CodeEntry> found = repository.findByCode("123456");
        assertTrue(found.isPresent());
        assertEquals("123456", found.get().getCode());
        assertEquals(OperatingSystem.WINDOWS, found.get().getServerOs());
        assertNotNull(found.get().getId());
    }

    @Test
    public void testDeleteByCode() {
        CodeEntry entry = new CodeEntry(null, "654321", Instant.now(), Instant.now().plusSeconds(3600), OperatingSystem.MACOS);
        repository.insert(entry);

        boolean deleted = repository.deleteByCode("654321");
        assertTrue(deleted);

        Optional<CodeEntry> found = repository.findByCode("654321");
        assertFalse(found.isPresent());
    }

    @Test
    public void testDeleteByCode_NotFound() {
        boolean deleted = repository.deleteByCode("nonexistent");
        assertFalse(deleted);
    }
}
