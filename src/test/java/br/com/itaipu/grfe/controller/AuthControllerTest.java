package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.LoginRequest;
import br.com.itaipu.grfe.dto.response.LoginResponse;
import br.com.itaipu.grfe.support.WebSecurityTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends WebSecurityTestBase {

    @Test
    void login_comCredenciaisValidas_retorna200ComToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse("token-jwt", "admin", "ROLE_ADMIN"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"senha\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    void login_comSenhaErrada_retorna401ComMensagem() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Usuário ou senha inválidos"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos"));
    }

    @Test
    void login_comCamposEmBranco_retorna400ComDetalhesDeValidacao() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Erro de validação nos dados enviados"))
                .andExpect(jsonPath("$.detalhes", hasSize(2)));
    }

    @Test
    void login_comJsonMalformado_retorna400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"admin\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void login_semCorpo_retorna400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}