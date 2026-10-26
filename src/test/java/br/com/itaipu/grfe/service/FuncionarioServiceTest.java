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
import java.util.Optional;
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

    @Test
    void listarTodos_mapeiaFuncionarios() {
        Funcionario funcionario = funcionario(1L, "Ana", "M1", "a@a.com",
                Set.of(especialidade(1L, "Java")));
        when(funcionarioRepository.findAll()).thenReturn(List.of(funcionario));

        var resposta = service.listarTodos();

        assertEquals(1, resposta.size());
        assertEquals("Ana", resposta.get(0).nome());
    }

    @Test
    void buscarPorId_quandoFuncionarioExiste_retornaResposta() {
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(
                funcionario(1L, "Ana", "M1", "a@a.com", Set.of())));

        assertEquals("Ana", service.buscarPorId(1L).nome());
    }

    @Test
    void buscarPorId_quandoFuncionarioInexiste_lancaEntidadeNaoEncontrada() {
        when(funcionarioRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscarPorId(1L));
    }

    @Test
    void atualizar_quandoFuncionarioExiste_atualizaDados() {
        Funcionario funcionario = funcionario(5L, "Velho", "X", "x@x.com", Set.of());
        Especialidades especialidade = especialidade(2L, "Banco de Dados");
        FuncionarioRequest request = new FuncionarioRequest(
                "Novo", "M2", "novo@email.com", Set.of(2L));
        when(funcionarioRepository.findById(5L)).thenReturn(Optional.of(funcionario));
        when(especialidadesRepository.findAllById(request.especialidadeIds()))
                .thenReturn(List.of(especialidade));
        when(funcionarioRepository.save(funcionario)).thenReturn(funcionario);

        FuncionarioResponse resposta = service.atualizar(5L, request);

        assertEquals("Novo", resposta.nome());
        assertEquals("M2", resposta.matricula());
        assertEquals(Set.of("Banco de Dados"), resposta.especialidades());
    }

    @Test
    void atualizar_quandoFuncionarioInexiste_naoBuscaEspecialidades() {
        when(funcionarioRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(
                5L, new FuncionarioRequest("Novo", "M2", "n@e.com", Set.of(2L))));
        verify(especialidadesRepository, never()).findAllById(any());
    }

    @Test
    void deletar_quandoFuncionarioExiste_removeRegistro() {
        when(funcionarioRepository.existsById(1L)).thenReturn(true);

        service.deletar(1L);

        verify(funcionarioRepository).deleteById(1L);
    }

    @Test
    void deletar_quandoFuncionarioInexiste_naoRemoveRegistro() {
        when(funcionarioRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.deletar(1L));
        verify(funcionarioRepository, never()).deleteById(any());
    }

    private Funcionario funcionario(Long id, String nome, String matricula, String email,
                                    Set<Especialidades> especialidades) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        funcionario.setNome(nome);
        funcionario.setMatricula(matricula);
        funcionario.setEmail(email);
        funcionario.setEspecialidades(especialidades);
        return funcionario;
    }

    private Especialidades especialidade(Long id, String nome) {
        Especialidades especialidade = new Especialidades();
        especialidade.setId(id);
        especialidade.setNome(nome);
        return especialidade;
    }
}
