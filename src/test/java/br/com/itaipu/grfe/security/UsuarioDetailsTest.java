package br.com.itaipu.grfe.security;

import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.support.Usuarios;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioDetailsTest {

    @Test
    void getAuthorities_roleAdmin_temPrefixoRole() {
        UsuarioDetails detalhes = Usuarios.detalhes("admin", Role.ADMIN);

        assertThat(detalhes.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void getAuthorities_roleUsuario_temPrefixoRole() {
        UsuarioDetails detalhes = Usuarios.detalhes("maria", Role.USUARIO);

        assertThat(detalhes.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USUARIO");
    }

    @Test
    void getUsernameEGetPassword_vindosDoUsuario() {
        UsuarioDetails detalhes = Usuarios.detalhes("admin", Role.ADMIN);

        assertThat(detalhes.getUsername()).isEqualTo("admin");
        assertThat(detalhes.getPassword()).isEqualTo("$2a$10$hash-de-teste");
        assertThat(detalhes.getId()).isEqualTo(1L);
    }

    @Test
    void contaSempreAtivaENaoExpirada() {
        UsuarioDetails detalhes = Usuarios.detalhes("admin", Role.ADMIN);

        assertThat(detalhes.isAccountNonExpired()).isTrue();
        assertThat(detalhes.isAccountNonLocked()).isTrue();
        assertThat(detalhes.isCredentialsNonExpired()).isTrue();
        assertThat(detalhes.isEnabled()).isTrue();
    }
}