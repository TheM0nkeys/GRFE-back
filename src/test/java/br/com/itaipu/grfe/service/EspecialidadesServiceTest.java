package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.EspecialidadesRequest;
import br.com.itaipu.grfe.dto.response.EspecialidadeResponse;
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

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EspecialidadesServiceTest {

    @Mock
    private EspecialidadesRepository especialidadesRepository;

    @Mock
    private DivisaoRepository divisaoRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    private EspecialidadesService service;

    @BeforeEach
    void setUp() {
        service = new EspecialidadesService(
                especialidadesRepository,
                divisaoRepository,
                departamentoRepository
        );
    }

    @Test
    void criar_quandoDepartamentoPertenceADivisao_salvaEspecialidade() {
        Divisao divisao = divisao(1L, "TI");
        Departamento departamento = departamento(10L, "Infraestrutura", divisao);
        EspecialidadesRequest request = new EspecialidadesRequest(
                "Redes", "Suporte de redes", 1L, 10L
        );

        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisao));
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento));
        when(especialidadesRepository.save(any(Especialidades.class))).thenAnswer(invocation -> {
            Especialidades especialidade = invocation.getArgument(0);
            especialidade.setId(100L);
            return especialidade;
        });

        EspecialidadeResponse response = service.criar(request);

        assertEquals(100L, response.id());
        assertEquals("Redes", response.nome());
        assertEquals(1L, response.divisaoId());
        assertEquals(10L, response.departamentoId());
        verify(especialidadesRepository).save(any(Especialidades.class));
    }

    @Test
    void criar_quandoDepartamentoNaoPertenceADivisao_lancaIllegalArgumentException() {
        Divisao divisaoInformada = divisao(1L, "TI");
        Divisao outraDivisao = divisao(2L, "Administrativa");
        Departamento departamento = departamento(10L, "Financeiro", outraDivisao);
        EspecialidadesRequest request = new EspecialidadesRequest(
                "Redes", "Suporte de redes", 1L, 10L
        );

        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisaoInformada));
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.criar(request)
        );

        assertEquals(
                "O departamento informado (Financeiro) não pertence à divisão informada (TI)",
                exception.getMessage()
        );
        verify(especialidadesRepository, never()).save(any());
    }

    @Test
    void listarTodas_mapeiaEspecialidades() {
        Divisao divisao = divisao(1L, "TI");
        Departamento departamento = departamento(10L, "Infraestrutura", divisao);
        when(especialidadesRepository.findAll()).thenReturn(List.of(
                especialidade(100L, "Redes", "desc", divisao, departamento)));

        var resposta = service.listarTodas();

        assertEquals(1, resposta.size());
        assertEquals("Redes", resposta.get(0).nome());
    }

    @Test
    void buscarPorId_quandoEspecialidadeExiste_retornaResposta() {
        Divisao divisao = divisao(1L, "TI");
        Departamento departamento = departamento(10L, "Infraestrutura", divisao);
        when(especialidadesRepository.findById(100L)).thenReturn(Optional.of(
                especialidade(100L, "Redes", "desc", divisao, departamento)));

        assertEquals("Redes", service.buscarPorId(100L).nome());
    }

    @Test
    void criar_quandoDivisaoNaoExiste_lancaEntidadeNaoEncontrada() {
        when(divisaoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.criar(new EspecialidadesRequest("Redes", "desc", 1L, 10L)));
        verify(departamentoRepository, never()).findById(any());
    }

    @Test
    void criar_quandoDepartamentoNaoExiste_lancaEntidadeNaoEncontrada() {
        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisao(1L, "TI")));
        when(departamentoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.criar(new EspecialidadesRequest("Redes", "desc", 1L, 10L)));
    }

    @Test
    void atualizar_quandoEspecialidadeInexistente_naoBuscaRelacionamentos() {
        when(especialidadesRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> service.atualizar(100L,
                        new EspecialidadesRequest("Nova", "desc", 1L, 10L)));
        verify(divisaoRepository, never()).findById(any());
    }

    @Test
    void deletar_quandoEspecialidadeExiste_removeRegistro() {
        when(especialidadesRepository.existsById(100L)).thenReturn(true);

        service.deletar(100L);

        verify(especialidadesRepository).deleteById(100L);
    }

    @Test
    void deletar_quandoEspecialidadeInexistente_naoRemoveRegistro() {
        when(especialidadesRepository.existsById(100L)).thenReturn(false);

        assertThrows(EntidadeNaoEncontradaException.class, () -> service.deletar(100L));
        verify(especialidadesRepository, never()).deleteById(any());
    }

    private Divisao divisao(Long id, String nome) {
        Divisao divisao = new Divisao();
        divisao.setId(id);
        divisao.setNome(nome);
        return divisao;
    }

    private Departamento departamento(Long id, String nome, Divisao divisao) {
        Departamento departamento = new Departamento();
        departamento.setId(id);
        departamento.setNome(nome);
        departamento.setDivisao(divisao);
        return departamento;
    }

    private Especialidades especialidade(Long id, String nome, String descricao,
                                           Divisao divisao, Departamento departamento) {
        Especialidades especialidade = new Especialidades();
        especialidade.setId(id);
        especialidade.setNome(nome);
        especialidade.setDescricao(descricao);
        especialidade.setDivisao(divisao);
        especialidade.setDepartamento(departamento);
        return especialidade;
    }
}
