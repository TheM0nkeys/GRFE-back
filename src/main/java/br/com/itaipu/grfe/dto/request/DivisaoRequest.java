package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.Divisao;
import jakarta.validation.constraints.NotBlank;

public record DivisaoRequest(
        @NotBlank(message = "O nome da divisão é obrigatório") String nome
) {
    public Divisao toEntity() {
        Divisao divisao = new Divisao();
        divisao.setNome(this.nome());
        return divisao;
    }
}