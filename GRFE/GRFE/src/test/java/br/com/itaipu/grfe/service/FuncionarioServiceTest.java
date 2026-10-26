package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.FuncionarioRequest;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FuncionarioServiceTest {
    @Mock FuncionarioRepository funcionarioRepository;
    @Mock EspecialidadesRepository especialidadesRepository;
    FuncionarioService service;
    @BeforeEach void setUp(){ service=new FuncionarioService(funcionarioRepository,especialidadesRepository); }

    @Test void listarTodos_mapeia(){ Funcionario f=funcionario(1L,"Ana","M1","a@a.com",Set.of(especialidade(1L,"Java"))); when(funcionarioRepository.findAll()).thenReturn(List.of(f)); var r=service.listarTodos(); assertEquals(1,r.size()); assertEquals("Ana",r.get(0).nome()); }
    @Test void buscarPorId_existente(){ Funcionario f=funcionario(1L,"Ana","M1","a@a.com",Set.of()); when(funcionarioRepository.findById(1L)).thenReturn(Optional.of(f)); assertEquals("Ana",service.buscarPorId(1L).nome()); }
    @Test void buscarPorId_inexistente(){ when(funcionarioRepository.findById(1L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.buscarPorId(1L)); }
    @Test void criar_todasEspecialidades_salva(){ var e1=especialidade(1L,"Java"); var e2=especialidade(2L,"BD"); var req=req("Felipe","123","f@e.com",Set.of(1L,2L)); when(especialidadesRepository.findAllById(req.especialidadeIds())).thenReturn(List.of(e1,e2)); when(funcionarioRepository.save(any())).thenAnswer(i->{Funcionario f=i.getArgument(0);f.setId(50L);return f;}); var r=service.criar(req); assertEquals(50L,r.id()); assertEquals(2,r.especialidades().size()); }
    @Test void criar_especialidadeAusente_lanca(){ var req=req("Felipe","123","f@e.com",Set.of(1L,2L)); when(especialidadesRepository.findAllById(req.especialidadeIds())).thenReturn(List.of(especialidade(1L,"Java"))); assertThrows(EntidadeNaoEncontradaException.class,()->service.criar(req)); verify(funcionarioRepository,never()).save(any()); }
    @Test void atualizar_existente_salvaTudo(){ Funcionario f=funcionario(5L,"Velho","X","x@x.com",Set.of()); var e=especialidade(2L,"BD"); var req=req("Novo","M2","novo@e.com",Set.of(2L)); when(funcionarioRepository.findById(5L)).thenReturn(Optional.of(f)); when(especialidadesRepository.findAllById(req.especialidadeIds())).thenReturn(List.of(e)); when(funcionarioRepository.save(f)).thenReturn(f); var r=service.atualizar(5L,req); assertEquals("Novo",r.nome()); assertEquals("M2",r.matricula()); assertEquals(Set.of("BD"),r.especialidades()); }
    @Test void atualizar_funcionarioAusente_lanca(){ when(funcionarioRepository.findById(5L)).thenReturn(Optional.empty()); assertThrows(EntidadeNaoEncontradaException.class,()->service.atualizar(5L,req("Novo","M2","n@e.com",Set.of(2L)))); verify(especialidadesRepository,never()).findAllById(any()); }
    @Test void atualizar_especialidadeAusente_lanca(){ Funcionario f=funcionario(5L,"Velho","X","x@x.com",Set.of()); var req=req("Novo","M2","n@e.com",Set.of(1L,2L)); when(funcionarioRepository.findById(5L)).thenReturn(Optional.of(f)); when(especialidadesRepository.findAllById(req.especialidadeIds())).thenReturn(List.of(especialidade(1L,"Java"))); assertThrows(EntidadeNaoEncontradaException.class,()->service.atualizar(5L,req)); verify(funcionarioRepository,never()).save(any()); }
    @Test void deletar_existente(){ when(funcionarioRepository.existsById(1L)).thenReturn(true); service.deletar(1L); verify(funcionarioRepository).deleteById(1L); }
    @Test void deletar_inexistente(){ when(funcionarioRepository.existsById(1L)).thenReturn(false); assertThrows(EntidadeNaoEncontradaException.class,()->service.deletar(1L)); verify(funcionarioRepository,never()).deleteById(any()); }

    private FuncionarioRequest req(String n,String m,String e,Set<Long> ids){ return new FuncionarioRequest(n,m,e,ids); }
    private Especialidades especialidade(Long id,String nome){ Especialidades e=new Especialidades();e.setId(id);e.setNome(nome);return e; }
    private Funcionario funcionario(Long id,String n,String m,String email,Set<Especialidades> esp){ Funcionario f=new Funcionario();f.setId(id);f.setNome(n);f.setMatricula(m);f.setEmail(email);f.setEspecialidades(esp);return f; }
}
