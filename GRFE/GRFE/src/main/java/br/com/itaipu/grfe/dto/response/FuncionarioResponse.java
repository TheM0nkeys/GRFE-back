package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.entity.Funcionario;

import java.util.Set;
import java.util.stream.Collectors;

public record FuncionarioResponse(
        Long id,
        String nome,
        String matricula,
        String email,
        Set<String> especialidades
) {
    public static FuncionarioResponse fromEntity(Funcionario funcionario) {
        return new FuncionarioResponse(
                funcionario.getId(),
                funcionario.getNome(),
                funcionario.getMatricula(),
                funcionario.getEmail(),
                funcionario.getEspecialidades().stream()
                        .map(e -> e.getNome())
                        .collect(Collectors.toSet())
        );
    }
}
