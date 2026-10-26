package br.com.itaipu.grfe.security;

import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.support.TestConstantes;
import br.com.itaipu.grfe.support.Usuarios;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService(TestConstantes.SEGREDO_JWT, TestConstantes.EXPIRACAO_MS);

    private final UsuarioDetails admin = Usuarios.detalhes("admin", Role.ADMIN);

    @Test
    void gerarToken_retornaJwtComTresPartes() {
        String token = jwtService.gerarToken(admin);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extrairLogin_retornaOLoginDoUsuarioDoToken() {
        String token = jwtService.gerarToken(admin);

        assertThat(jwtService.extrairLogin(token)).isEqualTo("admin");
    }

    @Test
    void tokenValido_comMesmoUsuario_retornaTrue() {
        String token = jwtService.gerarToken(admin);

        assertThat(jwtService.tokenValido(token, admin)).isTrue();
    }

    @Test
    void tokenValido_comUsuarioDiferente_retornaFalse() {
        String token = jwtService.gerarToken(admin);
        UsuarioDetails outro = Usuarios.detalhes("outro", Role.USUARIO);

        assertThat(jwtService.tokenValido(token, outro)).isFalse();
    }

    @Test
    void tokenExpirado_lancaExpiredJwtException() {
        JwtService jwtExpirado = new JwtService(TestConstantes.SEGREDO_JWT, -1_000L);
        String token = jwtExpirado.gerarToken(admin);

        assertThatThrownBy(() -> jwtService.extrairLogin(token))
                .isInstanceOf(ExpiredJwtException.class);
        assertThatThrownBy(() -> jwtService.tokenValido(token, admin))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void tokenAssinadoComOutraChave_lancaSignatureException() {
        JwtService outraChave = new JwtService(
                "outra-chave-totalmente-diferente-com-32-chars-ou-mais", TestConstantes.EXPIRACAO_MS);
        String tokenDeOutraChave = outraChave.gerarToken(admin);

        assertThatThrownBy(() -> jwtService.extrairLogin(tokenDeOutraChave))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void tokenMalformado_lancaMalformedJwtException() {
        assertThatThrownBy(() -> jwtService.extrairLogin("isto-nao-e-um-jwt"))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void construtor_comSegredoCurto_lancaWeakKeyException() {
        assertThatThrownBy(() -> new JwtService("curto", 1000L))
                .isInstanceOf(WeakKeyException.class);
    }
}