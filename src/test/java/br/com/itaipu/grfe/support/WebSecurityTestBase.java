package br.com.itaipu.grfe.support;

import br.com.itaipu.grfe.config.CorsConfig;
import br.com.itaipu.grfe.config.SecurityConfig;
import br.com.itaipu.grfe.controller.AuthController;
import br.com.itaipu.grfe.exception.GlobalExceptionHandler;
import br.com.itaipu.grfe.security.JwtAuthenticationFilter;
import br.com.itaipu.grfe.security.JwtService;
import br.com.itaipu.grfe.security.UsuarioDetailsService;
import br.com.itaipu.grfe.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(classes = {
        AuthController.class,
        ProbeController.class,
        SecurityConfig.class,
        CorsConfig.class,
        JwtAuthenticationFilter.class,
        JwtService.class,
        GlobalExceptionHandler.class
})
@TestPropertySource(properties = {
        "jwt.secret=" + TestConstantes.SEGREDO_JWT,
        "jwt.expiration-ms=" + TestConstantes.EXPIRACAO_MS
})
public abstract class WebSecurityTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected UsuarioDetailsService usuarioDetailsService;
}
