package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.Escala;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EscalaRequest(
        @NotNull(message = "A data/hora de início da vigência é obrigatória") LocalDateTime dataHoraInicio,
        @NotNull(message = "A data/hora de fim da vigência é obrigatória") LocalDateTime dataHoraFim,
        @NotNull(message = "A especialidade é obrigatória") Long especialidadeId,
        @NotNull(message = "O funcionário/plantonista é obrigatório") Long funcionarioId
) {
    public Escala toEntity() {
        Escala escala = new Escala();
        escala.setDataHoraInicio(this.dataHoraInicio());
        escala.setDataHoraFim(this.dataHoraFim());
        return escala;

    }
}
