package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.FuncionarioRequest;
import br.com.itaipu.grfe.dto.response.FuncionarioResponse;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private EspecialidadesRepository especialidadesRepository;

    private FuncionarioService service;

    @BeforeEach
    void setUp() {
        service = new FuncionarioService(funcionarioRepository, especialidadesRepository);
    }

    @Test
    void criar_quandoTodasEspecialidadesExistem_salvaFuncionario() {
        Especialidades java = especialidade(1L, "Java");
        Especialidades banco = especialidade(2L, "Banco de Dados");
        FuncionarioRequest request = new FuncionarioRequest(
                "Felipe", "12345", "felipe@email.com", Set.of(1L, 2L)
        );

        when(especialidadesRepository.findAllById(request.especialidadeIds()))
                .thenReturn(List.of(java, banco));
        when(funcionarioRepository.save(any(Funcionario.class))).thenAnswer(invocation -> {
            Funcionario funcionario = invocation.getArgument(0);
            funcionario.setId(50L);
            return funcionario;
        });

        FuncionarioResponse response = service.criar(request);

        assertEquals(50L, response.id());
        assertEquals("Felipe", response.nome());
        assertEquals(Set.of("Java", "Banco de Dados"), response.especialidades());
        verify(funcionarioRepository).save(any(Funcionario.class));
    }

    @Test
    void criar_quandoAlgumaEspecialidadeNaoExiste_lancaEntidadeNaoEncontrada() {
        FuncionarioRequest request = new FuncionarioRequest(
                "Felipe", "12345", "felipe@email.com", Set.of(1L, 2L)
        );

        when(especialidadesRepository.findAllById(request.especialidadeIds()))
                .thenReturn(List.of(especialidade(1L, "Java")));

        EntidadeNaoEncontradaException exception = assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> service.criar(request)
        );

        assertEquals("Uma ou mais especialidades informadas não existem", exception.getMessage());
        verify(funcionarioRepository, never()).save(any());
    }

    private Especialidades especialidade(Long id, String nome) {
        Especialidades especialidade = new Especialidades();
        especialidade.setId(id);
        especialidade.setNome(nome);
        return especialidade;
    }
}
