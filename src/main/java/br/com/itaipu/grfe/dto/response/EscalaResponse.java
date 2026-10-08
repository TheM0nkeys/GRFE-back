package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.Escala;

import java.time.LocalDateTime;

public record EscalaResponse(
        Long id,
        LocalDateTime dataHoraInicio,
        LocalDateTime dataHoraFim,
        Long especialidadeId,
        String especialidadeNome,
        Long funcionarioId,
        String funcionarioNome
) {
    public static EscalaResponse fromEntity(Escala escala) {
        return new EscalaResponse(
                escala.getId(),
                escala.getDataHoraInicio(),
                escala.getDataHoraFim(),
                escala.getEspecialidade().getId(),
                escala.getEspecialidade().getNome(),
                escala.getFuncionario().getId(),
                escala.getFuncionario().getNome()
        );
    }
}
