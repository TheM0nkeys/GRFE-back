package br.com.itaipu.grfe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 12, max = 72, message = "A senha deve ter entre 12 e 72 caracteres")
        String novaSenha
) {

    @Override
    public String toString() {
        return "RedefinirSenhaRequest[novaSenha=***]";
    }
}
