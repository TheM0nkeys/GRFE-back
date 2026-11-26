package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.Usuario;
import br.com.itaipu.grfe.entity.enums.Role;

public record UsuarioResponse(
        Long id,
        String login,
        Role role,
        Long funcionarioId,
        String funcionarioNome
) {

    public static UsuarioResponse fromEntity(Usuario usuario) {
        Funcionario funcionario = usuario.getFuncionario();
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getLogin(),
                usuario.getRole(),
                funcionario != null ? funcionario.getId() : null,
                funcionario != null ? funcionario.getNome() : null
        );
    }
}