package br.com.itaipu.grfe.security;

import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.repository.UsuarioRepository;
import br.com.itaipu.grfe.support.Usuarios;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void loadUserByUsername_usuarioExistente_retornaUserDetails() {
        when(usuarioRepository.findByLogin("admin"))
                .thenReturn(Optional.of(Usuarios.criar("admin", Role.ADMIN)));

        UserDetails detalhes = usuarioDetailsService.loadUserByUsername("admin");

        assertThat(detalhes.getUsername()).isEqualTo("admin");
        assertThat(detalhes).isInstanceOf(UsuarioDetails.class);
    }

    @Test
    void loadUserByUsername_usuarioInexistente_lancaUsernameNotFoundException() {
        when(usuarioRepository.findByLogin("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("fantasma"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("fantasma");
    }
}