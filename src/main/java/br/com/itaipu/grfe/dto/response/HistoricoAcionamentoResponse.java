package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.HistoricoAcionamento;

import java.time.LocalDateTime;

public record HistoricoAcionamentoResponse(
        Long id,
        LocalDateTime dataHora,
        Long autorId,
        String autorNome,
        String comentario
) {
    public static HistoricoAcionamentoResponse fromEntity(HistoricoAcionamento historico) {
        return new HistoricoAcionamentoResponse(
                historico.getId(),
                historico.getDataHora(),
                historico.getAutor().getId(),
                historico.getAutor().getNome(),
                historico.getComentario()
        );
    }
}
