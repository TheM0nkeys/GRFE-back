package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.Divisao;

public record DivisaoResponse(
        Long id,
        String nome
) {
    public static DivisaoResponse fromEntity(Divisao divisao) {
        return new DivisaoResponse(divisao.getId(), divisao.getNome());
    }
}
