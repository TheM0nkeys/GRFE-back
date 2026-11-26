package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "O login é obrigatório")
        @Size(min = 3, max = 50, message = "O login deve ter entre 3 e 50 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]+$",
                message = "O login só pode ter letras, números, ponto, hífen e sublinhado")
        String login,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 12, max = 72, message = "A senha deve ter entre 12 e 72 caracteres")
        String senha,

        @NotNull(message = "O perfil é obrigatório")
        Role role,

        @NotNull(message = "O funcionário vinculado é obrigatório")
        Long funcionarioId
) {

    @Override
    public String toString() {
        return "UsuarioRequest[login=" + login + ", role=" + role
                + ", funcionarioId=" + funcionarioId + ", senha=***]";
    }
}