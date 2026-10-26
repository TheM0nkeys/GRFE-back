package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.enums.StatusChamado;

import java.time.LocalDateTime;

public record ChamadoResponse(
        Long id,
        LocalDateTime dataHoraAcionamento,
        Long especialidadeId,
        String especialidadeNome,
        Long plantonistaId,
        String plantonistaNome,
        Long usuarioResponsavelId,
        String usuarioResponsavelNome,
        String motivo,
        String numeroIncidente,
        StatusChamado status
) {
    public static ChamadoResponse fromEntity(Chamado chamado) {
        return new ChamadoResponse(
                chamado.getId(),
                chamado.getDataHoraAcionamento(),
                chamado.getEspecialidade().getId(),
                chamado.getEspecialidade().getNome(),
                chamado.getPlantonista().getId(),
                chamado.getPlantonista().getNome(),
                chamado.getUsuarioResponsavel().getId(),
                chamado.getUsuarioResponsavel().getNome(),
                chamado.getMotivo(),
                chamado.getNumeroIncidente(),
                chamado.getStatus()
        );
    }
}
