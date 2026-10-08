package br.com.itaipu.grfe.dto.request;

import br.com.itaipu.grfe.entity.Funcionario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record FuncionarioRequest(
        @NotBlank(message = "O nome é obrigatório") String nome,
        @NotBlank(message = "A matrícula é obrigatória") String matricula,
        @Email(message = "E-mail inválido") String email,
        @NotEmpty(message = "Informe pelo menos uma especialidade") Set<Long> especialidadeIds
) {
    public Funcionario toEntity() {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(this.nome());
        funcionario.setMatricula(this.matricula());
        funcionario.setEmail(this.email());
        return funcionario;

    }
}
