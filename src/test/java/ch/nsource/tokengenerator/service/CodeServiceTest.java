package ch.nsource.tokengenerator.service;

import ch.nsource.tokengenerator.model.CodeEntry;
import ch.nsource.tokengenerator.model.OperatingSystem;
import ch.nsource.tokengenerator.repository.CodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CodeServiceTest {

    @Mock
    private CodeRepository repository;

    private CodeService codeService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        codeService = new CodeService(repository);
    }

    @Test
    public void testGenerateAndStore_Success() {
        when(repository.insert(any(CodeEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CodeEntry entry = codeService.generateAndStore(OperatingSystem.WINDOWS);

        assertNotNull(entry);
        assertNotNull(entry.getCode());
        assertEquals(6, entry.getCode().length());
        assertEquals(OperatingSystem.WINDOWS, entry.getServerOs());
        verify(repository, times(1)).insert(any(CodeEntry.class));
    }

    @Test
    public void testGenerateAndStore_WithRetry() {
        when(repository.insert(any(CodeEntry.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CodeEntry entry = codeService.generateAndStore(OperatingSystem.MACOS);

        assertNotNull(entry);
        assertEquals(OperatingSystem.MACOS, entry.getServerOs());
        verify(repository, times(2)).insert(any(CodeEntry.class));
    }

    @Test
    public void testGenerateAndStore_MaxRetriesExceeded() {
        when(repository.insert(any(CodeEntry.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(DataIntegrityViolationException.class, () -> {
            codeService.generateAndStore(OperatingSystem.WINDOWS);
        });

        verify(repository, times(10)).insert(any(CodeEntry.class));
    }

    @Test
    public void testGetIfValid_Valid() {
        CodeEntry entry = new CodeEntry(1L, "123456", Instant.now(), Instant.now().plusSeconds(3600), OperatingSystem.WINDOWS);
        when(repository.findByCode("123456")).thenReturn(Optional.of(entry));

        Optional<CodeEntry> result = codeService.getIfValid("123456");

        assertTrue(result.isPresent());
        assertEquals("123456", result.get().getCode());
        assertEquals(OperatingSystem.WINDOWS, result.get().getServerOs());
    }

    @Test
    public void testGetIfValid_Expired() {
        CodeEntry entry = new CodeEntry(1L, "123456", Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600), OperatingSystem.MACOS);
        when(repository.findByCode("123456")).thenReturn(Optional.of(entry));

        Optional<CodeEntry> result = codeService.getIfValid("123456");

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetIfValid_NotFound() {
        when(repository.findByCode("999999")).thenReturn(Optional.empty());

        Optional<CodeEntry> result = codeService.getIfValid("999999");

        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteCode() {
        when(repository.deleteByCode("123456")).thenReturn(true);
        assertTrue(codeService.deleteCode("123456"));

        when(repository.deleteByCode("999999")).thenReturn(false);
        assertFalse(codeService.deleteCode("999999"));
    }
}
