package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.response.RelatorioAcionamentosResponse;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import br.com.itaipu.grfe.repository.projecao.ContagemPorNome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private ChamadoRepository chamadoRepository;
    @InjectMocks
    private RelatorioService relatorioService;

    @Test
    void gerar_contabilizaESeparaChamadosNosQuatroAgrupamentos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 10, 20, 12, 0);
        when(chamadoRepository.contarNoPeriodo(inicio, fim)).thenReturn(3L);
        when(chamadoRepository.contarPorEspecialidade(inicio, fim)).thenReturn(List.of(
                new ContagemPorNome("Redes", 2L), new ContagemPorNome("Banco de Dados", 1L)));
        when(chamadoRepository.contarPorPlantonista(inicio, fim)).thenReturn(List.of(
                new ContagemPorNome("Alice", 2L), new ContagemPorNome("Bruno", 1L)));
        when(chamadoRepository.contarPorDivisao(inicio, fim)).thenReturn(List.of(
                new ContagemPorNome("Operações", 2L), new ContagemPorNome("Tecnologia", 1L)));
        when(chamadoRepository.contarPorDepartamento(inicio, fim)).thenReturn(List.of(
                new ContagemPorNome("Telecom", 2L), new ContagemPorNome("Dados", 1L)));

        RelatorioAcionamentosResponse resposta = relatorioService.gerar(inicio, fim);

        assertThat(resposta.periodoInicio()).isEqualTo(inicio);
        assertThat(resposta.periodoFim()).isEqualTo(fim);
        assertThat(resposta.totalAcionamentos()).isEqualTo(3);
        assertThat(resposta.porEspecialidade()).containsExactlyInAnyOrderEntriesOf(
                java.util.Map.of("Redes", 2L, "Banco de Dados", 1L));
        assertThat(resposta.porPlantonista()).containsExactlyInAnyOrderEntriesOf(
                java.util.Map.of("Alice", 2L, "Bruno", 1L));
        assertThat(resposta.porDivisao()).containsExactlyInAnyOrderEntriesOf(
                java.util.Map.of("Operações", 2L, "Tecnologia", 1L));
        assertThat(resposta.porDepartamento()).containsExactlyInAnyOrderEntriesOf(
                java.util.Map.of("Telecom", 2L, "Dados", 1L));
        verify(chamadoRepository).contarNoPeriodo(inicio, fim);
    }

    @Test
    void gerarSemanal_definePeriodoDeSeteDiasAteOMomentoDaGeracao() {
        prepararRelatorioVazio();
        LocalDateTime antes = LocalDateTime.now();

        RelatorioAcionamentosResponse resposta = relatorioService.gerarSemanal();

        LocalDateTime depois = LocalDateTime.now();
        assertThat(resposta.periodoFim()).isBetween(antes, depois);
        assertThat(resposta.periodoInicio()).isEqualTo(resposta.periodoFim().minusDays(7));
        assertThat(resposta.totalAcionamentos()).isZero();
        assertThat(resposta.porEspecialidade()).isEmpty();
    }

    @Test
    void gerarMensal_iniciaNoPrimeiroDiaDoMesEConsideraOMesAtual() {
        prepararRelatorioVazio();
        LocalDateTime antes = LocalDateTime.now();

        RelatorioAcionamentosResponse resposta = relatorioService.gerarMensal();

        LocalDateTime depois = LocalDateTime.now();
        assertThat(resposta.periodoFim()).isBetween(antes, depois);
        assertThat(resposta.periodoInicio()).isEqualTo(
                YearMonth.from(resposta.periodoFim()).atDay(1).atStartOfDay());
        assertThat(resposta.totalAcionamentos()).isZero();
        assertThat(resposta.porPlantonista()).isEmpty();
    }

    @Test
    void gerar_comPeriodoInvertido_lancaArgumentoInvalido() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 20, 12, 0);
        LocalDateTime fim = inicio.minusHours(1);

        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> relatorioService.gerar(inicio, fim)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A data final não pode ser anterior à data inicial.");
    }

    private void prepararRelatorioVazio() {
        when(chamadoRepository.contarNoPeriodo(any(), any())).thenReturn(0L);
        when(chamadoRepository.contarPorEspecialidade(any(), any())).thenReturn(List.of());
        when(chamadoRepository.contarPorPlantonista(any(), any())).thenReturn(List.of());
        when(chamadoRepository.contarPorDivisao(any(), any())).thenReturn(List.of());
        when(chamadoRepository.contarPorDepartamento(any(), any())).thenReturn(List.of());
    }
}
