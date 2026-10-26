package br.com.itaipu.grfe.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ErroResponse(
        LocalDateTime timestamp,
        int status,
        String mensagem,
        List<String> detalhes
) {
    public static ErroResponse of(int status, String mensagem) {
        return new ErroResponse(LocalDateTime.now(), status, mensagem, null);
    }

    public static ErroResponse of(int status, String mensagem, List<String> detalhes) {
        return new ErroResponse(LocalDateTime.now(), status, mensagem, detalhes);
    }
}
