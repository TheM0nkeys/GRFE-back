package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.response.RelatorioAcionamentosResponse;
import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

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
        when(chamadoRepository.findAll(any(Specification.class))).thenReturn(List.of(
                chamado("Redes", "Alice", "Operações", "Telecom"),
                chamado("Redes", "Alice", "Operações", "Telecom"),
                chamado("Banco de Dados", "Bruno", "Tecnologia", "Dados")
        ));

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
        verify(chamadoRepository).findAll(any(Specification.class));
    }

    @Test
    void gerarSemanal_definePeriodoDeSeteDiasAteOMomentoDaGeracao() {
        when(chamadoRepository.findAll(any(Specification.class))).thenReturn(List.of());
        LocalDateTime antes = LocalDateTime.now();

        RelatorioAcionamentosResponse resposta = relatorioService.gerarSemanal();

        LocalDateTime depois = LocalDateTime.now();
        assertThat(resposta.periodoFim()).isBetween(antes, depois);
        assertThat(resposta.periodoInicio()).isEqualTo(resposta.periodoFim().minusDays(7));
        assertThat(resposta.totalAcionamentos()).isZero();
        assertThat(resposta.porEspecialidade()).isEmpty();
        verify(chamadoRepository).findAll(any(Specification.class));
    }

    @Test
    void gerarMensal_iniciaNoPrimeiroDiaDoMesEConsideraOMesAtual() {
        when(chamadoRepository.findAll(any(Specification.class))).thenReturn(List.of());
        LocalDateTime antes = LocalDateTime.now();

        RelatorioAcionamentosResponse resposta = relatorioService.gerarMensal();

        LocalDateTime depois = LocalDateTime.now();
        assertThat(resposta.periodoFim()).isBetween(antes, depois);
        assertThat(resposta.periodoInicio()).isEqualTo(
                YearMonth.from(resposta.periodoFim()).atDay(1).atStartOfDay());
        assertThat(resposta.totalAcionamentos()).isZero();
        assertThat(resposta.porPlantonista()).isEmpty();
        verify(chamadoRepository).findAll(any(Specification.class));
    }

    private Chamado chamado(String especialidadeNome,
                            String plantonistaNome,
                            String divisaoNome,
                            String departamentoNome) {
        Divisao divisao = new Divisao();
        divisao.setNome(divisaoNome);
        Departamento departamento = new Departamento();
        departamento.setNome(departamentoNome);
        Especialidades especialidade = new Especialidades();
        especialidade.setNome(especialidadeNome);
        especialidade.setDivisao(divisao);
        especialidade.setDepartamento(departamento);
        Funcionario plantonista = new Funcionario();
        plantonista.setNome(plantonistaNome);
        Chamado chamado = new Chamado();
        chamado.setEspecialidade(especialidade);
        chamado.setPlantonista(plantonista);
        return chamado;
    }
}
