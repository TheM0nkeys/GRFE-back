package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.dto.request.DivisaoRequest;
import br.com.itaipu.grfe.entity.Divisao;
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
class DivisaoServiceTest {

    @Mock
    private DivisaoRepository divisaoRepository;

    private DivisaoService service;

    @BeforeEach
    void setUp() {
        service = new DivisaoService(divisaoRepository);
    }

    @Test
    void buscarPorId_quandoDivisaoNaoExiste_lancaEntidadeNaoEncontrada() {
        when(divisaoRepository.findById(55L)).thenReturn(Optional.empty());

        EntidadeNaoEncontradaException exception = assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> service.buscarPorId(55L)
        );

        assertEquals("Divisão não encontrada: 55", exception.getMessage());
    }

    @Test
    void deletar_quandoDivisaoNaoExiste_lancaEntidadeNaoEncontrada() {
        when(divisaoRepository.existsById(55L)).thenReturn(false);

        EntidadeNaoEncontradaException exception = assertThrows(
                EntidadeNaoEncontradaException.class,
                () -> service.deletar(55L)
        );

        assertEquals("Divisão não encontrada: 55", exception.getMessage());
        verify(divisaoRepository, never()).deleteById(55L);
    }

    @Test
    void listarTodas_retornaDivisoesMapeadas() {
        when(divisaoRepository.findAll()).thenReturn(java.util.List.of(
                divisao(1L, "TI"), divisao(2L, "ADM")));

        var resposta = service.listarTodas();

        assertEquals(2, resposta.size());
        assertEquals("TI", resposta.get(0).nome());
    }

    @Test
    void buscarPorId_quandoDivisaoExiste_retornaResposta() {
        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisao(1L, "TI")));

        assertEquals("TI", service.buscarPorId(1L).nome());
    }

    @Test
    void criar_salvaDivisao() {
        when(divisaoRepository.save(any())).thenAnswer(invocation -> {
            Divisao divisao = invocation.getArgument(0);
            divisao.setId(1L);
            return divisao;
        });

        var resposta = service.criar(new DivisaoRequest("TI"));

        assertEquals(1L, resposta.id());
        assertEquals("TI", resposta.nome());
    }

    @Test
    void atualizar_quandoDivisaoExiste_atualizaNome() {
        Divisao divisao = divisao(1L, "Velha");
        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisao));
        when(divisaoRepository.save(divisao)).thenReturn(divisao);

        assertEquals("Nova", service.atualizar(1L, new DivisaoRequest("Nova")).nome());
    }

    @Test
    void atualizar_quandoDivisaoNaoExiste_naoSalva() {
        when(divisaoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.atualizar(1L, new DivisaoRequest("Nova")));
        verify(divisaoRepository, never()).save(any());
    }

    @Test
    void deletar_quandoDivisaoExiste_removeRegistro() {
        when(divisaoRepository.existsById(1L)).thenReturn(true);

        service.deletar(1L);

        verify(divisaoRepository).deleteById(1L);
    }

    private Divisao divisao(Long id, String nome) {
        Divisao divisao = new Divisao();
        divisao.setId(id);
        divisao.setNome(nome);
        return divisao;
    }
}
