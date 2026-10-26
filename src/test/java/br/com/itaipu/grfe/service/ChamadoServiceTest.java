package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.ChamadoRequest;
import br.com.itaipu.grfe.dto.response.ChamadoResponse;
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
class ChamadoServiceTest {

    private static final LocalDateTime MOMENTO = LocalDateTime.of(2026, 10, 19, 10, 30);

    @Mock
    private ChamadoRepository chamadoRepository;
    @Mock
    private EspecialidadesRepository especialidadesRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @Mock
    private EscalaRepository escalaRepository;
    @InjectMocks
    private ChamadoService chamadoService;

    private Especialidades especialidade;
    private Funcionario plantonista;
    private Funcionario responsavel;

    @BeforeEach
    void configurarRelacoes() {
        especialidade = new Especialidades();
        especialidade.setId(10L);
        especialidade.setNome("Redes");

        plantonista = funcionario(20L, "Plantonista");
        plantonista.setEspecialidades(Set.of(especialidade));
        responsavel = funcionario(30L, "Responsável");
    }

    @Test
    void criar_comPlantonistaEspecializadoEEmEscala_vinculaRelacoes() {
        ChamadoRequest request = request();
        when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(especialidade));
        when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(plantonista));
        when(funcionarioRepository.findById(30L)).thenReturn(Optional.of(responsavel));
        when(escalaRepository.plantonistaEmEscala(10L, 20L, MOMENTO)).thenReturn(true);
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChamadoResponse resposta = chamadoService.criar(request);

        ArgumentCaptor<Chamado> chamadoCaptor = ArgumentCaptor.forClass(Chamado.class);
        verify(chamadoRepository).save(chamadoCaptor.capture());
        assertThat(chamadoCaptor.getValue().getEspecialidade()).isSameAs(especialidade);
        assertThat(chamadoCaptor.getValue().getPlantonista()).isSameAs(plantonista);
        assertThat(chamadoCaptor.getValue().getUsuarioResponsavel()).isSameAs(responsavel);
        assertThat(resposta.status()).isEqualTo(StatusChamado.ABERTO);
        verify(escalaRepository).plantonistaEmEscala(10L, 20L, MOMENTO);
    }

    @Test
    void criar_comEspecialidadeInexistente_lancaNaoEncontrada() {
        when(especialidadesRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.criar(request()))
                .isInstanceOf(EntidadeNaoEncontradaException.class)
                .hasMessage("Especialidade não encontrada: 10");

        verify(funcionarioRepository, never()).findById(any());
    }

    @Test
    void criar_comPlantonistaInexistente_lancaNaoEncontrado() {
        when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(especialidade));
        when(funcionarioRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.criar(request()))
                .isInstanceOf(EntidadeNaoEncontradaException.class)
                .hasMessage("Plantonista não encontrado: 20");

        verify(escalaRepository, never()).plantonistaEmEscala(any(), any(), any());
    }

    @Test
    void criar_quandoPlantonistaNaoPossuiEspecialidade_lancaArgumentoInvalido() {
        plantonista.setEspecialidades(Set.of());
        when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(especialidade));
        when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(plantonista));

        assertThatThrownBy(() -> chamadoService.criar(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O plantonista informado não possui a especialidade do chamado.");

        verify(escalaRepository, never()).plantonistaEmEscala(any(), any(), any());
    }

    @Test
    void criar_quandoPlantonistaEstaForaDaEscala_lancaArgumentoInvalido() {
        prepararPlantonista();
        when(escalaRepository.plantonistaEmEscala(10L, 20L, MOMENTO)).thenReturn(false);

        assertThatThrownBy(() -> chamadoService.criar(request()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("O plantonista informado não está em escala para esta especialidade na data/hora do acionamento.");

        verify(funcionarioRepository, never()).findById(30L);
    }

    @Test
    void criar_comResponsavelInexistente_lancaNaoEncontrado() {
        prepararPlantonista();
        when(escalaRepository.plantonistaEmEscala(10L, 20L, MOMENTO)).thenReturn(true);
        when(funcionarioRepository.findById(30L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.criar(request()))
                .isInstanceOf(EntidadeNaoEncontradaException.class)
                .hasMessage("Usuário responsável não encontrado: 30");
    }

    @Test
    void atualizarStatus_atualizaEDevolveChamado() {
        Chamado chamado = chamadoExistente();
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(chamado)).thenReturn(chamado);

        ChamadoResponse resposta = chamadoService.atualizarStatus(1L, StatusChamado.FECHADO);

        assertThat(chamado.getStatus()).isEqualTo(StatusChamado.FECHADO);
        assertThat(resposta.status()).isEqualTo(StatusChamado.FECHADO);
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void atualizarStatus_comChamadoInexistente_lancaNaoEncontrado() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.atualizarStatus(99L, StatusChamado.FECHADO))
                .isInstanceOf(EntidadeNaoEncontradaException.class)
                .hasMessage("Acionamento não encontrado: 99");

        verify(chamadoRepository, never()).save(any());
    }

    @Test
    void listar_mapeiaResultados() {
        Chamado chamado = chamadoExistente();
        when(chamadoRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Chamado>>any()))
                .thenReturn(java.util.List.of(chamado));

        var resposta = chamadoService.listar(
                StatusChamado.ABERTO, 10L, 20L, null, null,
                MOMENTO.minusDays(1), MOMENTO.plusDays(1));

        assertThat(resposta).hasSize(1);
        assertThat(resposta.get(0).especialidadeNome()).isEqualTo("Redes");
    }

    @Test
    void buscarPorId_comChamadoExistente_retornaResposta() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamadoExistente()));

        assertThat(chamadoService.buscarPorId(1L).id()).isEqualTo(1L);
    }

    @Test
    void atualizar_comChamadoExistente_atualizaDados() {
        Chamado chamado = chamadoExistente();
        ChamadoRequest request = new ChamadoRequest(
                MOMENTO.plusHours(1), 10L, 20L, 30L,
                "Novo motivo", "INC-999", StatusChamado.FECHADO);
        prepararPlantonista();
        when(escalaRepository.plantonistaEmEscala(10L, 20L, MOMENTO.plusHours(1))).thenReturn(true);
        when(funcionarioRepository.findById(30L)).thenReturn(Optional.of(responsavel));
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(chamado)).thenReturn(chamado);

        ChamadoResponse resposta = chamadoService.atualizar(1L, request);

        assertThat(resposta.motivo()).isEqualTo("Novo motivo");
        assertThat(resposta.numeroIncidente()).isEqualTo("INC-999");
        assertThat(resposta.status()).isEqualTo(StatusChamado.FECHADO);
    }

    @Test
    void atualizar_comChamadoInexistente_lancaNaoEncontrado() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chamadoService.atualizar(99L, request()))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
        verify(especialidadesRepository, never()).findById(any());
    }

    @Test
    void deletar_comChamadoExistente_removeRegistro() {
        when(chamadoRepository.existsById(1L)).thenReturn(true);

        chamadoService.deletar(1L);

        verify(chamadoRepository).deleteById(1L);
    }

    @Test
    void deletar_comChamadoInexistente_lancaNaoEncontrado() {
        when(chamadoRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> chamadoService.deletar(99L))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
        verify(chamadoRepository, never()).deleteById(any());
    }

    private void prepararPlantonista() {
        when(especialidadesRepository.findById(10L)).thenReturn(Optional.of(especialidade));
        when(funcionarioRepository.findById(20L)).thenReturn(Optional.of(plantonista));
    }

    private ChamadoRequest request() {
        return new ChamadoRequest(MOMENTO, 10L, 20L, 30L, "Falha de rede", "INC-123", StatusChamado.ABERTO);
    }

    private Chamado chamadoExistente() {
        Chamado chamado = new Chamado();
        chamado.setId(1L);
        chamado.setDataHoraAcionamento(MOMENTO);
        chamado.setEspecialidade(especialidade);
        chamado.setPlantonista(plantonista);
        chamado.setUsuarioResponsavel(responsavel);
        chamado.setMotivo("Falha de rede");
        chamado.setStatus(StatusChamado.ABERTO);
        return chamado;
    }

    private Funcionario funcionario(Long id, String nome) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        funcionario.setNome(nome);
        return funcionario;
    }
}