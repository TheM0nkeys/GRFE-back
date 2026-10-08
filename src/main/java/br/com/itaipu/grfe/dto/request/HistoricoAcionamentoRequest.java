package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.HistoricoAcionamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record HistoricoAcionamentoRequest(
        @NotNull(message = "A data/hora da atualização é obrigatória") LocalDateTime dataHora,
        @NotNull(message = "O autor da atualização é obrigatório") Long autorId,
        @NotBlank(message = "O comentário é obrigatório") String comentario
) {
    public HistoricoAcionamento toEntity() {
        HistoricoAcionamento historico = new HistoricoAcionamento();
        historico.setDataHora(this.dataHora());
        historico.setComentario(this.comentario());
        return historico;

    }
}
