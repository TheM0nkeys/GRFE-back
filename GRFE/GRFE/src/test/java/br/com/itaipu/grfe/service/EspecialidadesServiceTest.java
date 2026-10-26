package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.EspecialidadesRequest;
import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.DepartamentoRepository;
import br.com.itaipu.grfe.repository.DivisaoRepository;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
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
class EspecialidadesServiceTest {
    @Mock EspecialidadesRepository especialidadesRepository;
    @Mock DivisaoRepository divisaoRepository;
    @Mock DepartamentoRepository departamentoRepository;
    EspecialidadesService service;

    @BeforeEach void setUp(){ service=new EspecialidadesService(especialidadesRepository,divisaoRepository,departamentoRepository); }

    @Test void listarTodas_mapeia(){ Divisao d=divisao(1L,"TI"); Departamento dep=departamento(10L,"Infra",d); when(especialidadesRepository.findAll()).thenReturn(List.of(especialidade(100L,"Redes","desc",d,dep))); var r=service.listarTodas(); assertEquals(1,r.size()); assertEquals("Redes",r.get(0).nome()); }
    @Test void buscarPorId_existente(){ Divisao d=divisao(1L,"TI"); Departamento dep=departamento(10L,"Infra",d); when(especialidadesRepository.findById(100L)).thenReturn(Optional.of(especialidade(100L,"Redes","desc",d,dep))); assertEquals("Redes",service.buscarPorId(100L).nome()); }
    @Test void buscarPorId_inexistente(){ when(especialidadesRepository.findById(100L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.buscarPorId(100L)); }
    @Test void criar_valido_salva(){ Divisao d=divisao(1L,"TI"); Departamento dep=departamento(10L,"Infra",d); var req=req("Redes","desc",1L,10L); when(divisaoRepository.findById(1L)).thenReturn(Optional.of(d)); when(departamentoRepository.findById(10L)).thenReturn(Optional.of(dep)); when(especialidadesRepository.save(any())).thenAnswer(i->{Especialidades e=i.getArgument(0);e.setId(100L);return e;}); var r=service.criar(req); assertEquals(100L,r.id()); assertEquals(1L,r.divisaoId()); assertEquals(10L,r.departamentoId()); }
    @Test void criar_divisaoAusente_lanca(){ when(divisaoRepository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req("Redes","d",1L,10L))); verify(departamentoRepository,never()).findById(any()); }
    @Test void criar_departamentoAusente_lanca(){ Divisao d=divisao(1L,"TI"); when(divisaoRepository.findById(1L)).thenReturn(Optional.of(d)); when(departamentoRepository.findById(10L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req("Redes","d",1L,10L))); }
    @Test void criar_departamentoDeOutraDivisao_lanca(){ Divisao d1=divisao(1L,"TI"), d2=divisao(2L,"ADM"); Departamento dep=departamento(10L,"Financeiro",d2); when(divisaoRepository.findById(1L)).thenReturn(Optional.of(d1)); when(departamentoRepository.findById(10L)).thenReturn(Optional.of(dep)); var e=assertThrows(IllegalArgumentException.class,()->service.criar(req("Redes","d",1L,10L))); assertTrue(e.getMessage().contains("não pertence")); }
    @Test void atualizar_valido(){ Divisao d=divisao(1L,"TI"); Departamento dep=departamento(10L,"Infra",d); Especialidades esp=especialidade(100L,"Antiga","old",d,dep); when(especialidadesRepository.findById(100L)).thenReturn(Optional.of(esp)); when(divisaoRepository.findById(1L)).thenReturn(Optional.of(d)); when(departamentoRepository.findById(10L)).thenReturn(Optional.of(dep)); when(especialidadesRepository.save(esp)).thenReturn(esp); var r=service.atualizar(100L,req("Nova","new",1L,10L)); assertEquals("Nova",r.nome()); assertEquals("new",r.descricao()); }
    @Test void atualizar_inexistente_lanca(){ when(especialidadesRepository.findById(100L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.atualizar(100L,req("N","d",1L,10L))); verify(divisaoRepository,never()).findById(any()); }
    @Test void deletar_existente(){ when(especialidadesRepository.existsById(100L)).thenReturn(true); service.deletar(100L); verify(especialidadesRepository).deleteById(100L); }
    @Test void deletar_inexistente(){ when(especialidadesRepository.existsById(100L)).thenReturn(false); assertThrows(EntidadeNaoEncontradaException.class,()->service.deletar(100L)); verify(especialidadesRepository,never()).deleteById(any()); }

    private EspecialidadesRequest req(String n,String desc,Long d,Long dep){ return new EspecialidadesRequest(n,desc,d,dep); }
    private Divisao divisao(Long id,String n){ Divisao d=new Divisao();d.setId(id);d.setNome(n);return d; }
    private Departamento departamento(Long id,String n,Divisao d){ Departamento dep=new Departamento();dep.setId(id);dep.setNome(n);dep.setDivisao(d);return dep; }
    private Especialidades especialidade(Long id,String n,String desc,Divisao d,Departamento dep){ Especialidades e=new Especialidades();e.setId(id);e.setNome(n);e.setDescricao(desc);e.setDivisao(d);e.setDepartamento(dep);return e; }
}
