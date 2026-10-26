package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.HistoricoAcionamentoRequest;
import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.HistoricoAcionamento;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import br.com.itaipu.grfe.repository.HistoricoAcionamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricoAcionamentoServiceTest {
    @Mock HistoricoAcionamentoRepository historicoRepository;
    @Mock ChamadoRepository chamadoRepository;
    @Mock FuncionarioRepository funcionarioRepository;
    @InjectMocks HistoricoAcionamentoService service;

    @Test void listarPorChamado_ordena(){ Funcionario a=funcionario(1L,"Ana"), b=funcionario(2L,"Bruno"); var recente=hist(2L,LocalDateTime.of(2026,1,2,10,0),b,"B"); var antigo=hist(1L,LocalDateTime.of(2026,1,1,10,0),a,"A"); Chamado c=chamado(5L); c.setHistorico(new HashSet<>(Set.of(recente,antigo))); when(chamadoRepository.findById(5L)).thenReturn(Optional.of(c)); var r=service.listarPorChamado(5L); assertEquals(2,r.size()); assertEquals("Ana",r.get(0).autorNome()); assertEquals("Bruno",r.get(1).autorNome()); }
    @Test void listarPorChamado_inexistente(){ when(chamadoRepository.findById(404L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.listarPorChamado(404L)); }
    @Test void registrar_valido_salvaEVincula(){ Chamado c=chamado(5L); Funcionario a=funcionario(7L,"Ana"); var req=new HistoricoAcionamentoRequest(LocalDateTime.of(2026,1,1,12,0),7L,"ok"); when(chamadoRepository.findById(5L)).thenReturn(Optional.of(c)); when(funcionarioRepository.findById(7L)).thenReturn(Optional.of(a)); when(historicoRepository.save(any())).thenAnswer(i->{HistoricoAcionamento h=i.getArgument(0);h.setId(9L);return h;}); var r=service.registrar(5L,req); assertEquals(9L,r.id()); assertEquals("Ana",r.autorNome()); assertEquals(1,c.getHistorico().size()); verify(historicoRepository).save(any()); }
    @Test void registrar_chamadoAusente(){ when(chamadoRepository.findById(5L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.registrar(5L,new HistoricoAcionamentoRequest(LocalDateTime.now(),7L,"ok"))); verify(funcionarioRepository,never()).findById(any()); }
    @Test void registrar_autorAusente(){ Chamado c=chamado(5L); when(chamadoRepository.findById(5L)).thenReturn(Optional.of(c)); when(funcionarioRepository.findById(7L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.registrar(5L,new HistoricoAcionamentoRequest(LocalDateTime.now(),7L,"ok"))); verify(historicoRepository,never()).save(any()); }
    private Chamado chamado(Long id){ Chamado c=new Chamado();c.setId(id);c.setHistorico(new HashSet<>());return c; }
    private Funcionario funcionario(Long id,String n){ Funcionario f=new Funcionario();f.setId(id);f.setNome(n);return f; }
    private HistoricoAcionamento hist(Long id,LocalDateTime dt,Funcionario a,String c){ HistoricoAcionamento h=new HistoricoAcionamento();h.setId(id);h.setDataHora(dt);h.setAutor(a);h.setComentario(c);return h; }
}
