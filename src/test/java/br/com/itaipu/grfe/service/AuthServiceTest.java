package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.LoginRequest;
import br.com.itaipu.grfe.dto.response.LoginResponse;
import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.security.JwtService;
import br.com.itaipu.grfe.security.UsuarioDetails;
import br.com.itaipu.grfe.support.Usuarios;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_comCredenciaisValidas_retornaTokenLoginERole() {
        UsuarioDetails usuario = Usuarios.detalhes("admin", Role.ADMIN);
        Authentication autenticacao =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(autenticacao);
        when(jwtService.gerarToken(usuario)).thenReturn("token-jwt");

        LoginResponse resposta = authService.login(new LoginRequest("admin", "Admin@123"));

        assertThat(resposta.token()).isEqualTo("token-jwt");
        assertThat(resposta.login()).isEqualTo("admin");
        assertThat(resposta.role()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void login_comRoleUsuario_retornaRoleComPrefixo() {
        UsuarioDetails usuario = Usuarios.detalhes("maria", Role.USUARIO);
        Authentication autenticacao =
                new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(autenticacao);
        when(jwtService.gerarToken(usuario)).thenReturn("token-maria");

        LoginResponse resposta = authService.login(new LoginRequest("maria", "senha123"));

        assertThat(resposta.role()).isEqualTo("ROLE_USUARIO");
    }

    @Test
    void login_comSenhaErrada_lancaBadCredentialsComMensagemTratada() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin", "errada")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Usuário ou senha inválidos");

        verify(jwtService, never()).gerarToken(any());
    }
}