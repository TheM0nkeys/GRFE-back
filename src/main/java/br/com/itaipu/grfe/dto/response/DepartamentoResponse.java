package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.Departamento;

public record DepartamentoResponse(
        Long id,
        String nome,
        Long divisaoId,
        String divisaoNome
) {
    public static DepartamentoResponse fromEntity(Departamento departamento) {
        return new DepartamentoResponse(
                departamento.getId(),
                departamento.getNome(),
                departamento.getDivisao().getId(),
                departamento.getDivisao().getNome()
        );
    }
}
