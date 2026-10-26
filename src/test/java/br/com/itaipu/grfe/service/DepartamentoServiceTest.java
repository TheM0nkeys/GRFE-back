package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.DepartamentoRequest;
import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.DepartamentoRepository;
import br.com.itaipu.grfe.repository.DivisaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private DivisaoRepository divisaoRepository;

    private DepartamentoService service;

    @BeforeEach
    void setUp() {
        service = new DepartamentoService(departamentoRepository, divisaoRepository);
    }

    @Test
    void criar_quandoDivisaoNaoExiste_lancaEntidadeNaoEncontrada() {
        DepartamentoRequest request = new DepartamentoRequest("Infraestrutura", 99L);
        when(divisaoRepository.findById(99L)).thenReturn(Optional.empty());

        EntidadeNaoEncontradaException exception = assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> service.criar(request)
        );

        assertEquals("Divisão não encontrada: 99", exception.getMessage());
        verify(departamentoRepository, never()).save(any());
    }

    @Test
    void buscarPorId_quandoDepartamentoNaoExiste_lancaEntidadeNaoEncontrada() {
        when(departamentoRepository.findById(77L)).thenReturn(Optional.empty());

        EntidadeNaoEncontradaException exception = assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> service.buscarPorId(77L)
        );

        assertEquals("Departamento não encontrado: 77", exception.getMessage());
    }

    @Test
    void listarTodos_mapeiaEntidades() {
        Divisao divisao = divisao(1L, "TI");
        when(departamentoRepository.findAll()).thenReturn(java.util.List.of(
                departamento(10L, "Infra", divisao)));

        var resposta = service.listarTodos();

        assertEquals(1, resposta.size());
        assertEquals("Infra", resposta.get(0).nome());
        assertEquals(1L, resposta.get(0).divisaoId());
    }

    @Test
    void buscarPorId_quandoDepartamentoExiste_retornaResposta() {
        Divisao divisao = divisao(1L, "TI");
        when(departamentoRepository.findById(10L))
                .thenReturn(Optional.of(departamento(10L, "Infra", divisao)));

        assertEquals("Infra", service.buscarPorId(10L).nome());
    }

    @Test
    void criar_quandoDivisaoExiste_salvaDepartamento() {
        Divisao divisao = divisao(1L, "TI");
        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisao));
        when(departamentoRepository.save(any())).thenAnswer(invocation -> {
            Departamento departamento = invocation.getArgument(0);
            departamento.setId(10L);
            return departamento;
        });

        var resposta = service.criar(new DepartamentoRequest("Infra", 1L));

        assertEquals(10L, resposta.id());
        assertEquals("Infra", resposta.nome());
        assertEquals("TI", resposta.divisaoNome());
    }

    @Test
    void atualizar_quandoDepartamentoExiste_atualizaNomeEDivisao() {
        Divisao antiga = divisao(1L, "Antiga");
        Divisao nova = divisao(2L, "Nova");
        Departamento departamento = departamento(10L, "Velho", antiga);
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento));
        when(divisaoRepository.findById(2L)).thenReturn(Optional.of(nova));
        when(departamentoRepository.save(departamento)).thenReturn(departamento);

        var resposta = service.atualizar(10L, new DepartamentoRequest("Novo", 2L));

        assertEquals("Novo", resposta.nome());
        assertEquals(2L, resposta.divisaoId());
    }

    @Test
    void atualizar_quandoDepartamentoNaoExiste_naoBuscaDivisao() {
        when(departamentoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.atualizar(10L, new DepartamentoRequest("Novo", 2L)));
        verify(divisaoRepository, never()).findById(any());
    }

    @Test
    void deletar_quandoDepartamentoExiste_removeRegistro() {
        when(departamentoRepository.existsById(10L)).thenReturn(true);

        service.deletar(10L);

        verify(departamentoRepository).deleteById(10L);
    }

    @Test
    void deletar_quandoDepartamentoNaoExiste_naoRemoveRegistro() {
        when(departamentoRepository.existsById(10L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.deletar(10L));
        verify(departamentoRepository, never()).deleteById(any());
    }

    private Divisao divisao(Long id, String nome) {
        Divisao divisao = new Divisao();
        divisao.setId(id);
        divisao.setNome(nome);
        return divisao;
    }

    private Departamento departamento(Long id, String nome, Divisao divisao) {
        Departamento departamento = new Departamento();
        departamento.setId(id);
        departamento.setNome(nome);
        departamento.setDivisao(divisao);
        return departamento;
    }
}
