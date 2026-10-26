package br.com.itaipu.grfe.dto.response;

public record LoginResponse(
        String token,
        String login,
        String role
) {
}