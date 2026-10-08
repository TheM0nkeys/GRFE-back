package br.com.itaipu.grfe.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O login é obrigatório") String login,
        @NotBlank(message = "A senha é obrigatória") String senha
) {
}