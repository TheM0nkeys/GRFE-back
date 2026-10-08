package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ChamadoRequest(
        @NotNull(message = "A data/hora do acionamento é obrigatória") LocalDateTime dataHoraAcionamento,
        @NotNull(message = "A especialidade acionada é obrigatória") Long especialidadeId,
        @NotNull(message = "O plantonista acionado é obrigatório") Long plantonistaId,
        @NotNull(message = "O usuário responsável pelo acionamento é obrigatório") Long usuarioResponsavelId,
        @NotBlank(message = "O motivo do acionamento é obrigatório") String motivo,
        String numeroIncidente,
        @NotNull(message = "O status do acionamento é obrigatório") StatusChamado status
) {
    public Chamado toEntity() {
        Chamado chamado = new Chamado();
        chamado.setDataHoraAcionamento(this.dataHoraAcionamento());
        chamado.setMotivo(this.motivo());
        chamado.setNumeroIncidente(this.numeroIncidente());
        chamado.setStatus(this.status());
        return chamado;

    }
}
