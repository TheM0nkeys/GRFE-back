package br.com.itaipu.grfe.exception;

import br.com.itaipu.grfe.support.ProbeController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("security-test")
@ContextConfiguration(classes = {ProbeController.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void badCredentialsException_retorna401ComCorpoPadrao() throws Exception {
        mockMvc.perform(get("/teste/credenciais"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void accessDeniedException_retorna403ComCorpoPadrao() throws Exception {
        mockMvc.perform(get("/teste/negado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para acessar este recurso"))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}