package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.DivisaoRequest;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
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
class DivisaoServiceTest {
    @Mock DivisaoRepository repository;
    DivisaoService service;
    @BeforeEach void setUp(){ service=new DivisaoService(repository); }

    @Test void listarTodas_retornaMapeado(){ when(repository.findAll()).thenReturn(List.of(divisao(1L,"TI"),divisao(2L,"ADM"))); var r=service.listarTodas(); assertEquals(2,r.size()); assertEquals("TI",r.get(0).nome()); }
    @Test void buscarPorId_existente(){ when(repository.findById(1L)).thenReturn(Optional.of(divisao(1L,"TI"))); assertEquals("TI",service.buscarPorId(1L).nome()); }
    @Test void buscarPorId_inexistente(){ when(repository.findById(55L)).thenReturn(Optional.empty()); var e=assertThrows(EntidadeNaoEncontradaException.class,()->service.buscarPorId(55L)); assertEquals("Divisão não encontrada: 55",e.getMessage()); }
    @Test void criar_salva(){ when(repository.save(any())).thenAnswer(i->{Divisao d=i.getArgument(0);d.setId(1L);return d;}); var r=service.criar(new DivisaoRequest("TI")); assertEquals(1L,r.id()); assertEquals("TI",r.nome()); }
    @Test void atualizar_existente(){ Divisao d=divisao(1L,"Velha"); when(repository.findById(1L)).thenReturn(Optional.of(d)); when(repository.save(d)).thenReturn(d); var r=service.atualizar(1L,new DivisaoRequest("Nova")); assertEquals("Nova",r.nome()); }
    @Test void atualizar_inexistente(){ when(repository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.atualizar(1L,new DivisaoRequest("Nova"))); verify(repository,never()).save(any()); }
    @Test void deletar_existente(){ when(repository.existsById(1L)).thenReturn(true); service.deletar(1L); verify(repository).deleteById(1L); }
    @Test void deletar_inexistente(){ when(repository.existsById(55L)).thenReturn(false); assertThrows(EntidadeNaoEncontradaException.class,()->service.deletar(55L)); verify(repository,never()).deleteById(any()); }
    private Divisao divisao(Long id,String nome){ Divisao d=new Divisao();d.setId(id);d.setNome(nome);return d; }
}
