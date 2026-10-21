package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.DepartamentoRequest;
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
}
