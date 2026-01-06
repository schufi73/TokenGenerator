package ch.nsource.tokengenerator.web;

import ch.nsource.tokengenerator.model.CodeEntry;
import ch.nsource.tokengenerator.service.CodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CodeService codeService;

    @Test
    public void testCreateCode_Success() throws Exception {
        CodeEntry entry = new CodeEntry(1L, "123456", Instant.now(), Instant.now().plusSeconds(3600));
        when(codeService.generateAndStore()).thenReturn(entry);

        mockMvc.perform(post("/codes"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("123456"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    public void testGetCode_Success() throws Exception {
        CodeEntry entry = new CodeEntry(1L, "123456", Instant.now(), Instant.now().plusSeconds(3600));
        when(codeService.getIfValid("123456")).thenReturn(Optional.of(entry));

        mockMvc.perform(get("/codes/123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("123456"));
    }

    @Test
    public void testGetCode_NotFound() throws Exception {
        when(codeService.getIfValid("999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/codes/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Code not found or expired"));
    }

    @Test
    public void testDeleteCode_Success() throws Exception {
        when(codeService.deleteCode("123456")).thenReturn(true);

        mockMvc.perform(delete("/codes/123456"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteCode_NotFound() throws Exception {
        when(codeService.deleteCode("999999")).thenReturn(false);

        mockMvc.perform(delete("/codes/999999"))
                .andExpect(status().isNotFound());
    }
}
