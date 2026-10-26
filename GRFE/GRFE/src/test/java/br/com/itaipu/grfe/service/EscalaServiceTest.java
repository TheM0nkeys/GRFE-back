package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.EscalaRequest;
import br.com.itaipu.grfe.entity.Escala;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
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
class EscalaServiceTest {
    static final LocalDateTime INICIO=LocalDateTime.of(2026,10,20,8,0), FIM=LocalDateTime.of(2026,10,20,18,0);
    @Mock EscalaRepository escalaRepository;
    @Mock EspecialidadesRepository especialidadesRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @InjectMocks EscalaService service;
    Especialidades esp; Funcionario func;

    @BeforeEach void setUp(){ esp=new Especialidades();esp.setId(10L);esp.setNome("Redes"); func=new Funcionario();func.setId(20L);func.setNome("Ana");func.setEspecialidades(Set.of(esp)); }

    @Test void listarTodas_mapeia(){ Escala e=escala(1L); when(escalaRepository.findAll()).thenReturn(List.of(e)); var r=service.listarTodas(); assertEquals(1,r.size()); assertEquals(10L,r.get(0).especialidadeId()); }
    @Test void buscarPorId_existente(){ when(escalaRepository.findById(1L)).thenReturn(Optional.of(escala(1L))); assertEquals(1L,service.buscarPorId(1L).id()); }
    @Test void buscarPorId_inexistente(){ when(escalaRepository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.buscarPorId(1L)); }
    @Test void criar_intervaloInvertido(){ assertThrows(IllegalArgumentException.class,()->service.criar(req(INICIO,INICIO.minusSeconds(1)))); verify(especialidadesRepository,never()).findById(any()); }
    @Test void criar_intervaloIgual(){ assertThrows(IllegalArgumentException.class,()->service.criar(req(INICIO,INICIO))); verify(escalaRepository,never()).save(any()); }
    @Test void criar_valido(){ preparar(); when(escalaRepository.save(any())).thenAnswer(i->{Escala e=i.getArgument(0);e.setId(1L);return e;}); var r=service.criar(req(INICIO,FIM)); assertEquals(1L,r.id()); assertEquals("Ana",r.funcionarioNome()); }
    @Test void criar_especialidadeAusente(){ when(especialidadesRepository.findById(10L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req(INICIO,FIM))); verify(funcionarioRepository,never()).findById(any()); }
    @Test void criar_funcionarioAusente(){ when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(esp)); when(funcionarioRepository.findById(20L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req(INICIO,FIM))); }
    @Test void criar_funcionarioSemEspecialidade(){ func.setEspecialidades(Set.of()); preparar(); assertThrows(IllegalArgumentException.class,()->service.criar(req(INICIO,FIM))); verify(escalaRepository,never()).save(any()); }
    @Test void atualizar_intervaloInvalidoAntesDaBusca(){ assertThrows(IllegalArgumentException.class,()->service.atualizar(1L,req(INICIO,INICIO))); verify(escalaRepository,never()).findById(any()); }
    @Test void atualizar_escalaAusente(){ when(escalaRepository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.atualizar(1L,req(INICIO,FIM))); }
    @Test void atualizar_valido(){ Escala e=escala(1L); when(escalaRepository.findById(1L)).thenReturn(Optional.of(e)); preparar(); when(escalaRepository.save(e)).thenReturn(e); var r=service.atualizar(1L,req(INICIO,FIM.plusHours(1))); assertEquals(FIM.plusHours(1),r.dataHoraFim()); }
    @Test void deletar_existente(){ when(escalaRepository.existsById(1L)).thenReturn(true); service.deletar(1L); verify(escalaRepository).deleteById(1L); }
    @Test void deletar_inexistente(){ when(escalaRepository.existsById(1L)).thenReturn(false); assertThrows(EntidadeNaoEncontradaException.class,()->service.deletar(1L)); verify(escalaRepository,never()).deleteById(any()); }

    private void preparar(){ when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(esp)); when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(func)); }
    private EscalaRequest req(LocalDateTime i,LocalDateTime f){ return new EscalaRequest(i,f,10L,20L); }
    private Escala escala(Long id){ Escala e=new Escala();e.setId(id);e.setDataHoraInicio(INICIO);e.setDataHoraFim(FIM);e.setEspecialidade(esp);e.setFuncionario(func);return e; }
}
