package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.Especialidades;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EspecialidadesRequest(
        @NotBlank(message = "O nome da especialidade é obrigatório") String nome,
        String descricao,
        @NotNull(message = "A divisão é obrigatória") Long divisaoId,
        @NotNull(message = "O departamento é obrigatório") Long departamentoId
) {
    public Especialidades toEntity() {
        Especialidades especialidade = new Especialidades();
        especialidade.setNome(this.nome());
        especialidade.setDescricao(this.descricao());
        return especialidade;

    }
}
