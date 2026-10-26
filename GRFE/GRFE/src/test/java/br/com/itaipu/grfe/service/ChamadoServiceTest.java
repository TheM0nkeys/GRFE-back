package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.ChamadoRequest;
import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import br.com.itaipu.grfe.repository.EscalaRepository;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {
    static final LocalDateTime MOMENTO=LocalDateTime.of(2026,10,19,10,30);
    @Mock ChamadoRepository chamadoRepository;
    @Mock EspecialidadesRepository especialidadesRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @Mock EscalaRepository escalaRepository;
    @InjectMocks ChamadoService service;
    Especialidades esp; Funcionario plantonista; Funcionario responsavel;

    @BeforeEach void setUp(){ esp=new Especialidades();esp.setId(10L);esp.setNome("Redes"); plantonista=funcionario(20L,"Plant");plantonista.setEspecialidades(Set.of(esp)); responsavel=funcionario(30L,"Resp"); }

    @Test void listar_mapeiaResultados(){ Chamado c=chamado(1L); when(chamadoRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Chamado>>any())).thenReturn(List.of(c)); var r=service.listar(StatusChamado.ABERTO,10L,20L,null,null,MOMENTO.minusDays(1),MOMENTO.plusDays(1)); assertEquals(1,r.size()); assertEquals("Redes",r.get(0).especialidadeNome()); }
    @Test void buscarPorId_existente(){ when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado(1L))); assertEquals(1L,service.buscarPorId(1L).id()); }
    @Test void buscarPorId_inexistente(){ when(chamadoRepository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.buscarPorId(1L)); }
    @Test void atualizarStatus_valido(){ Chamado c=chamado(1L); when(chamadoRepository.findById(1L)).thenReturn(Optional.of(c)); when(chamadoRepository.save(c)).thenReturn(c); assertEquals(StatusChamado.FECHADO,service.atualizarStatus(1L,StatusChamado.FECHADO).status()); }
    @Test void atualizarStatus_inexistente(){ when(chamadoRepository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.atualizarStatus(1L,StatusChamado.FECHADO)); }
    @Test void criar_valido(){ prepararRelacoes(); when(chamadoRepository.save(any())).thenAnswer(i->{Chamado c=i.getArgument(0);c.setId(1L);return c;}); var r=service.criar(req()); assertEquals(1L,r.id()); assertEquals(StatusChamado.ABERTO,r.status()); }
    @Test void criar_especialidadeAusente(){ when(especialidadesRepository.findById(10L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req())); }
    @Test void criar_plantonistaAusente(){ when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(esp)); when(funcionarioRepository.findById(20L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req())); }
    @Test void criar_plantonistaSemEspecialidade(){ plantonista.setEspecialidades(Set.of()); when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(esp)); when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(plantonista)); assertThrows(IllegalArgumentException.class,()->service.criar(req())); }
    @Test void criar_foraDaEscala(){ prepararAtePlantonista(); when(escalaRepository.plantonistaEmEscala(10L,20L,MOMENTO)).thenReturn(false); assertThrows(IllegalArgumentException.class,()->service.criar(req())); }
    @Test void criar_responsavelAusente(){ prepararAtePlantonista(); when(escalaRepository.plantonistaEmEscala(10L,20L,MOMENTO)).thenReturn(true); when(funcionarioRepository.findById(30L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req())); }
    @Test void atualizar_valido(){ Chamado c=chamado(1L); when(chamadoRepository.findById(1L)).thenReturn(Optional.of(c)); prepararRelacoes(); when(chamadoRepository.save(c)).thenReturn(c); var novo=new ChamadoRequest(MOMENTO.plusHours(1),10L,20L,30L,"Novo motivo","INC-999",StatusChamado.FECHADO); var r=service.atualizar(1L,novo); assertEquals("Novo motivo",r.motivo()); assertEquals("INC-999",r.numeroIncidente()); assertEquals(StatusChamado.FECHADO,r.status()); }
    @Test void atualizar_inexistente(){ when(chamadoRepository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.atualizar(1L,req())); verify(especialidadesRepository,never()).findById(any()); }
    @Test void deletar_existente(){ when(chamadoRepository.existsById(1L)).thenReturn(true); service.deletar(1L); verify(chamadoRepository).deleteById(1L); }
    @Test void deletar_inexistente(){ when(chamadoRepository.existsById(1L)).thenReturn(false); assertThrows(EntidadeNaoEncontradaException.class,()->service.deletar(1L)); verify(chamadoRepository,never()).deleteById(any()); }

    private void prepararAtePlantonista(){ when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(esp)); when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(plantonista)); }
    private void prepararRelacoes(){ prepararAtePlantonista(); when(escalaRepository.plantonistaEmEscala(eq(10L),eq(20L),any(LocalDateTime.class))).thenReturn(true); when(funcionarioRepository.findById(30L)).thenReturn(Optional.of(responsavel)); }
    private ChamadoRequest req(){ return new ChamadoRequest(MOMENTO,10L,20L,30L,"Falha","INC-123",StatusChamado.ABERTO); }
    private Funcionario funcionario(Long id,String n){ Funcionario f=new Funcionario();f.setId(id);f.setNome(n);return f; }
    private Chamado chamado(Long id){ Chamado c=new Chamado();c.setId(id);c.setDataHoraAcionamento(MOMENTO);c.setEspecialidade(esp);c.setPlantonista(plantonista);c.setUsuarioResponsavel(responsavel);c.setMotivo("Falha");c.setNumeroIncidente("INC-123");c.setStatus(StatusChamado.ABERTO);return c; }
}
