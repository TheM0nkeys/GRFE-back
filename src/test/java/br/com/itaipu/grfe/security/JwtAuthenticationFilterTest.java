package br.com.itaipu.grfe.security;

import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.support.TestConstantes;
import br.com.itaipu.grfe.support.Usuarios;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService =
            new JwtService(TestConstantes.SEGREDO_JWT, TestConstantes.EXPIRACAO_MS);

    private UsuarioDetailsService usuarioDetailsService;
    private JwtAuthenticationFilter filtro;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        usuarioDetailsService = mock(UsuarioDetailsService.class);
        filtro = new JwtAuthenticationFilter(jwtService, usuarioDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void semHeaderAuthorization_seguePeloFluxoSemAutenticar() throws Exception {
        filtro.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void headerSemPrefixoBearer_seguePeloFluxoSemAutenticar() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpzZW5oYQ==");

        filtro.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenInvalido_filtroEngoleAExcecaoESegueOFluxo() throws Exception {
        request.addHeader("Authorization", "Bearer token.invalido.qualquer");

        filtro.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenExpirado_filtroEngoleAExcecaoESegueOFluxo() throws Exception {
        JwtService jwtExpirado = new JwtService(TestConstantes.SEGREDO_JWT, -1_000L);
        String token = jwtExpirado.gerarToken(Usuarios.detalhes("admin", Role.ADMIN));
        request.addHeader("Authorization", "Bearer " + token);

        filtro.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenValidoDeUsuarioInexistente_filtroEngoleAExcecaoESegueOFluxo() throws Exception {
        UsuarioDetails fantasma = Usuarios.detalhes("fantasma", Role.USUARIO);
        request.addHeader("Authorization", "Bearer " + jwtService.gerarToken(fantasma));
        when(usuarioDetailsService.loadUserByUsername("fantasma"))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado: fantasma"));

        filtro.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenValidoMasDeOutroUsuario_naoAutentica() throws Exception {
        String tokenDeAdmin = jwtService.gerarToken(Usuarios.detalhes("admin", Role.ADMIN));
        request.addHeader("Authorization", "Bearer " + tokenDeAdmin);
        when(usuarioDetailsService.loadUserByUsername("admin"))
                .thenReturn(Usuarios.detalhes("outro", Role.USUARIO));

        filtro.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void tokenValido_populaOSecurityContextComUsuarioEAuthority() throws Exception {
        UsuarioDetails admin = Usuarios.detalhes("admin", Role.ADMIN);
        request.addHeader("Authorization", "Bearer " + jwtService.gerarToken(admin));
        when(usuarioDetailsService.loadUserByUsername("admin")).thenReturn(admin);

        filtro.doFilter(request, response, chain);

        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        assertThat(chain.getRequest()).isNotNull();
        assertThat(autenticacao).isNotNull();
        assertThat(autenticacao.isAuthenticated()).isTrue();
        assertThat(autenticacao.getName()).isEqualTo("admin");
        assertThat(autenticacao.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }
}