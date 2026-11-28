package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.HistoricoAcionamento;
import jakarta.validation.constraints.NotBlank;

public record HistoricoAcionamentoRequest(
        @NotBlank(message = "O comentário é obrigatório") String comentario
) {
    public HistoricoAcionamento toEntity() {
        HistoricoAcionamento historico = new HistoricoAcionamento();
        historico.setComentario(this.comentario());
        return historico;

    }
}
