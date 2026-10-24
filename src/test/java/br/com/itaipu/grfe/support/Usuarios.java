package br.com.itaipu.grfe.support;

import br.com.itaipu.grfe.entity.Usuario;
import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.security.UsuarioDetails;

public final class Usuarios {

    private Usuarios() {
    }

    public static Usuario criar(String login, Role role) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setLogin(login);
        usuario.setSenhaHash("$2a$10$hash-de-teste");
        usuario.setRole(role);
        return usuario;
    }

    public static UsuarioDetails detalhes(String login, Role role) {
        return new UsuarioDetails(criar(login, role));
    }
}