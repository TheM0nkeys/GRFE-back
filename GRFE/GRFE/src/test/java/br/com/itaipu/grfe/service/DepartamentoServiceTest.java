package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.DepartamentoRequest;
import br.com.itaipu.grfe.dto.response.DepartamentoResponse;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartamentoServiceTest {
    @Mock DepartamentoRepository departamentoRepository;
    @Mock DivisaoRepository divisaoRepository;
    DepartamentoService service;

    @BeforeEach void setUp() { service = new DepartamentoService(departamentoRepository, divisaoRepository); }

    @Test void listarTodos_mapeiaEntidades() {
        Divisao d = divisao(1L, "TI");
        when(departamentoRepository.findAll()).thenReturn(List.of(departamento(10L, "Infra", d)));
        List<DepartamentoResponse> r = service.listarTodos();
        assertEquals(1, r.size()); assertEquals("Infra", r.get(0).nome()); assertEquals(1L, r.get(0).divisaoId());
    }

    @Test void buscarPorId_existente_retorna() {
        Divisao d = divisao(1L, "TI");
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento(10L,"Infra",d)));
        assertEquals("Infra", service.buscarPorId(10L).nome());
    }

    @Test void buscarPorId_quandoDepartamentoNaoExiste_lancaEntidadeNaoEncontrada() {
        when(departamentoRepository.findById(77L)).thenReturn(Optional.empty());
        var e = assertThrows(EntidadeNaoEncontradaException.class, () -> service.buscarPorId(77L));
        assertEquals("Departamento não encontrado: 77", e.getMessage());
    }

    @Test void criar_comDivisaoExistente_salva() {
        Divisao d = divisao(1L,"TI"); DepartamentoRequest req = new DepartamentoRequest("Infra",1L);
        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(d));
        when(departamentoRepository.save(any())).thenAnswer(i -> { Departamento dep=i.getArgument(0); dep.setId(10L); return dep; });
        DepartamentoResponse r = service.criar(req);
        assertEquals(10L,r.id()); assertEquals("Infra",r.nome()); assertEquals("TI",r.divisaoNome());
    }

    @Test void criar_quandoDivisaoNaoExiste_lancaEntidadeNaoEncontrada() {
        DepartamentoRequest request = new DepartamentoRequest("Infraestrutura", 99L);
        when(divisaoRepository.findById(99L)).thenReturn(Optional.empty());
        var e = assertThrows(EntidadeNaoEncontradaException.class, () -> service.criar(request));
        assertEquals("Divisão não encontrada: 99", e.getMessage()); verify(departamentoRepository, never()).save(any());
    }

    @Test void atualizar_existente_alteraNomeEDivisao() {
        Divisao antiga=divisao(1L,"Antiga"), nova=divisao(2L,"Nova"); Departamento dep=departamento(10L,"Velho",antiga);
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(dep));
        when(divisaoRepository.findById(2L)).thenReturn(Optional.of(nova));
        when(departamentoRepository.save(dep)).thenReturn(dep);
        var r=service.atualizar(10L,new DepartamentoRequest("Novo",2L));
        assertEquals("Novo",r.nome()); assertEquals(2L,r.divisaoId());
    }

    @Test void atualizar_departamentoInexistente_lanca() {
        when(departamentoRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(10L,new DepartamentoRequest("Novo",2L)));
        verify(divisaoRepository,never()).findById(any());
    }

    @Test void atualizar_divisaoInexistente_lanca() {
        Divisao antiga=divisao(1L,"Antiga"); Departamento dep=departamento(10L,"Velho",antiga);
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(dep));
        when(divisaoRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntidadeNaoEncontradaException.class, () -> service.atualizar(10L,new DepartamentoRequest("Novo",2L)));
        verify(departamentoRepository,never()).save(any());
    }

    @Test void deletar_existente_exclui() {
        when(departamentoRepository.existsById(10L)).thenReturn(true); service.deletar(10L); verify(departamentoRepository).deleteById(10L);
    }

    @Test void deletar_inexistente_lanca() {
        when(departamentoRepository.existsById(10L)).thenReturn(false);
        assertThrows(EntidadeNaoEncontradaException.class, () -> service.deletar(10L)); verify(departamentoRepository,never()).deleteById(any());
    }

    private Divisao divisao(Long id,String nome){ Divisao d=new Divisao(); d.setId(id); d.setNome(nome); return d; }
    private Departamento departamento(Long id,String nome,Divisao d){ Departamento x=new Departamento(); x.setId(id); x.setNome(nome); x.setDivisao(d); return x; }
}
