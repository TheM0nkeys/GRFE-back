package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.DivisaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
