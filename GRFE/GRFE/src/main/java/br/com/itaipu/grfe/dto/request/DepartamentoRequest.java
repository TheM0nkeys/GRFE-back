package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.Departamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DepartamentoRequest(
        @NotBlank(message = "O nome do departamento é obrigatório") String nome,
        @NotNull(message = "A divisão é obrigatória") Long divisaoId
) {
    public Departamento toEntity() {
        Departamento departamento = new Departamento();
        departamento.setNome(this.nome());
        return departamento;

    }
}