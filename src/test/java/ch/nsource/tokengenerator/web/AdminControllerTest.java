package ch.nsource.tokengenerator.web;

import ch.nsource.tokengenerator.service.LoginAttemptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Test
    public void testLogin_Success() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .contentType("application/json")
                        .header("X-Forwarded-For", "10.0.1.1")
                        .content("{\"password\":\"NetSource2025!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testLogin_WrongPassword() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .contentType("application/json")
                        .header("X-Forwarded-For", "10.0.1.2")
                        .content("{\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid password"));
    }

    @Test
    public void testLogin_MissingPassword() throws Exception {
        mockMvc.perform(post("/admin/login")
                        .contentType("application/json")
                        .header("X-Forwarded-For", "10.0.1.3")
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid password"));
    }

    @Test
    public void testLogin_RateLimited() throws Exception {
        String ip = "10.0.1.4";
        // Exhaust the 5 allowed attempts
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/admin/login")
                    .contentType("application/json")
                    .header("X-Forwarded-For", ip)
                    .content("{\"password\":\"wrong\"}"));
        }
        // Next attempt — even with correct password — should be blocked
        mockMvc.perform(post("/admin/login")
                        .contentType("application/json")
                        .header("X-Forwarded-For", ip)
                        .content("{\"password\":\"NetSource2025!\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Too many failed attempts. Try again later."));
    }

    @Test
    public void testLogin_SuccessResetsRateLimit() throws Exception {
        String ip = "10.0.1.5";
        // Accumulate some failures
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/admin/login")
                    .contentType("application/json")
                    .header("X-Forwarded-For", ip)
                    .content("{\"password\":\"wrong\"}"));
        }
        // Successful login resets the counter
        mockMvc.perform(post("/admin/login")
                        .contentType("application/json")
                        .header("X-Forwarded-For", ip)
                        .content("{\"password\":\"NetSource2025!\"}"))
                .andExpect(status().isOk());

        // Should not be blocked after reset
        mockMvc.perform(post("/admin/login")
                        .contentType("application/json")
                        .header("X-Forwarded-For", ip)
                        .content("{\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }
}
