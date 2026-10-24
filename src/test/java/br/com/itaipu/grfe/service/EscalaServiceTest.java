package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.EscalaRequest;
import br.com.itaipu.grfe.dto.response.EscalaResponse;
import br.com.itaipu.grfe.entity.Escala;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.repository.EscalaRepository;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EscalaServiceTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 20, 8, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 10, 20, 18, 0);
    private static final String ERRO_INTERVALO =
            "A data/hora de fim deve ser posterior à data/hora de início.";

    @Mock
    private EscalaRepository escalaRepository;
    @Mock
    private EspecialidadesRepository especialidadesRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @InjectMocks
    private EscalaService escalaService;

    private Especialidades especialidade;
    private Funcionario funcionario;

    @BeforeEach
    void configurarRelacoes() {
        especialidade = new Especialidades();
        especialidade.setId(10L);
        especialidade.setNome("Redes");
        funcionario = new Funcionario();
        funcionario.setId(20L);
        funcionario.setNome("Ana");
        funcionario.setEspecialidades(Set.of(especialidade));
    }

    @Test
    void criar_comFimAnteriorAoInicio_rejeitaEnaoSalva() {
        assertThatThrownBy(() -> escalaService.criar(request(INICIO, INICIO.minusSeconds(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERRO_INTERVALO);

        verify(escalaRepository, never()).save(any(Escala.class));
        verify(especialidadesRepository, never()).findById(any());
    }

    @Test
    void criar_comDatasIguais_rejeitaEnaoSalva() {
        assertThatThrownBy(() -> escalaService.criar(request(INICIO, INICIO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERRO_INTERVALO);

        verify(escalaRepository, never()).save(any(Escala.class));
    }

    @Test
    void criar_comFimPosteriorEFuncionarioEspecializado_salvaEscala() {
        prepararVinculos();
        when(escalaRepository.save(any(Escala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EscalaResponse resposta = escalaService.criar(request(INICIO, FIM));

        ArgumentCaptor<Escala> escalaCaptor = ArgumentCaptor.forClass(Escala.class);
        verify(escalaRepository).save(escalaCaptor.capture());
        assertThat(escalaCaptor.getValue().getDataHoraInicio()).isEqualTo(INICIO);
        assertThat(escalaCaptor.getValue().getDataHoraFim()).isEqualTo(FIM);
        assertThat(escalaCaptor.getValue().getEspecialidade()).isSameAs(especialidade);
        assertThat(escalaCaptor.getValue().getFuncionario()).isSameAs(funcionario);
        assertThat(resposta.especialidadeNome()).isEqualTo("Redes");
        assertThat(resposta.funcionarioNome()).isEqualTo("Ana");
    }

    @Test
    void criar_quandoFuncionarioNaoPossuiEspecialidade_rejeitaOperacao() {
        funcionario.setEspecialidades(Set.of());
        prepararVinculos();

        assertThatThrownBy(() -> escalaService.criar(request(INICIO, FIM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O funcionário informado não possui a especialidade selecionada.");

        verify(escalaRepository, never()).save(any(Escala.class));
    }

    @Test
    void atualizar_comFimAnteriorAoInicio_rejeitaAntesDeBuscarEscala() {
        assertThatThrownBy(() -> escalaService.atualizar(1L, request(INICIO, INICIO.minusSeconds(1))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERRO_INTERVALO);

        verify(escalaRepository, never()).findById(1L);
        verify(escalaRepository, never()).save(any(Escala.class));
    }

    @Test
    void atualizar_comDatasIguais_rejeitaAntesDeBuscarEscala() {
        assertThatThrownBy(() -> escalaService.atualizar(1L, request(INICIO, INICIO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERRO_INTERVALO);

        verify(escalaRepository, never()).findById(1L);
        verify(escalaRepository, never()).save(any(Escala.class));
    }

    @Test
    void atualizar_comFimPosteriorEFuncionarioEspecializado_salvaAlteracoes() {
        Escala existente = new Escala();
        existente.setId(1L);
        when(escalaRepository.findById(1L)).thenReturn(Optional.of(existente));
        prepararVinculos();
        when(escalaRepository.save(any(Escala.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EscalaResponse resposta = escalaService.atualizar(1L, request(INICIO, FIM));

        assertThat(resposta.dataHoraInicio()).isEqualTo(INICIO);
        assertThat(resposta.dataHoraFim()).isEqualTo(FIM);
        assertThat(resposta.especialidadeId()).isEqualTo(10L);
        assertThat(resposta.funcionarioId()).isEqualTo(20L);
        verify(escalaRepository).save(existente);
    }

    @Test
    void atualizar_quandoFuncionarioNaoPossuiEspecialidade_rejeitaOperacao() {
        Escala existente = new Escala();
        existente.setId(1L);
        when(escalaRepository.findById(1L)).thenReturn(Optional.of(existente));
        funcionario.setEspecialidades(Set.of());
        prepararVinculos();

        assertThatThrownBy(() -> escalaService.atualizar(1L, request(INICIO, FIM)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O funcionário informado não possui a especialidade selecionada.");

        verify(escalaRepository, never()).save(any(Escala.class));
    }

    private void prepararVinculos() {
        when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(especialidade));
        when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(funcionario));
    }

    private EscalaRequest request(LocalDateTime inicio, LocalDateTime fim) {
        return new EscalaRequest(inicio, fim, 10L, 20L);
    }
}
