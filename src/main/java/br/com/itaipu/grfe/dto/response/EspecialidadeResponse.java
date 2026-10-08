package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.Especialidades;

public record EspecialidadeResponse(
        Long id,
        String nome,
        String descricao,
        Long divisaoId,
        String divisaoNome,
        Long departamentoId,
        String departamentoNome
) {
    public static EspecialidadeResponse fromEntity(Especialidades especialidade) {
        return new EspecialidadeResponse(
                especialidade.getId(),
                especialidade.getNome(),
                especialidade.getDescricao(),
                especialidade.getDivisao().getId(),
                especialidade.getDivisao().getNome(),
                especialidade.getDepartamento().getId(),
                especialidade.getDepartamento().getNome()
        );
    }
}
