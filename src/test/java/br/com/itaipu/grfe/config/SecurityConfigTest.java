package br.com.itaipu.grfe.config;

import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.security.JwtService;
import br.com.itaipu.grfe.security.UsuarioDetails;
import br.com.itaipu.grfe.support.TestConstantes;
import br.com.itaipu.grfe.support.Usuarios;
import br.com.itaipu.grfe.support.WebSecurityTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityConfigTest extends WebSecurityTestBase {

    @Autowired
    private JwtService jwtService;

    private UsuarioDetails admin;

    @BeforeEach
    void setUp() {
        admin = Usuarios.detalhes("admin", Role.ADMIN);
        when(usuarioDetailsService.loadUserByUsername("admin")).thenReturn(admin);
    }

    @Test
    void rotaProtegida_semToken_retorna401() throws Exception {
        mockMvc.perform(get("/teste/protegido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegida_comTokenValido_retorna200() throws Exception {
        String token = jwtService.gerarToken(admin);

        mockMvc.perform(get("/teste/protegido").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void rotaProtegida_comTokenAdulterado_retorna401() throws Exception {
        String token = jwtService.gerarToken(admin);
        String adulterado = token.substring(0, token.length() - 3) + "xyz";

        mockMvc.perform(get("/teste/protegido").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegida_comTokenExpirado_retorna401() throws Exception {
        String expirado = new JwtService(TestConstantes.SEGREDO_JWT, -1_000L).gerarToken(admin);

        mockMvc.perform(get("/teste/protegido").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegida_comTokenDeUsuarioQueNaoExisteMais_retorna401() throws Exception {
        UsuarioDetails fantasma = Usuarios.detalhes("fantasma", Role.USUARIO);
        String token = jwtService.gerarToken(fantasma);
        when(usuarioDetailsService.loadUserByUsername("fantasma"))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado: fantasma"));

        mockMvc.perform(get("/teste/protegido").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegida_comHeaderSemBearer_retorna401() throws Exception {
        mockMvc.perform(get("/teste/protegido").header("Authorization", "Basic dXNlcjpzZW5oYQ=="))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void swaggerUiHtml_semToken_estaLiberado() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    void apiDocs_semToken_estaLiberado() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void preflightDoAngular_naoPrecisaDeToken_eLiberaAOrigem() throws Exception {
        mockMvc.perform(options("/teste/protegido")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void preflightDeOrigemNaoPermitida_eRejeitado() throws Exception {
        mockMvc.perform(options("/teste/protegido")
                        .header("Origin", "http://site-desconhecido.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}