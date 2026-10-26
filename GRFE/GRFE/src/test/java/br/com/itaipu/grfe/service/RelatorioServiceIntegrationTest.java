package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RelatorioServiceIntegrationTest {

    @Autowired
    private RelatorioService relatorioService;
    @Autowired
    private EntityManager entityManager;

    private Especialidades especialidade;
    private Funcionario plantonista;

    @BeforeEach
    void prepararRelacoes() {
        Divisao divisao = new Divisao();
        divisao.setNome("Operações");
        entityManager.persist(divisao);

        Departamento departamento = new Departamento();
        departamento.setNome("Redes");
        departamento.setDivisao(divisao);
        entityManager.persist(departamento);

        especialidade = new Especialidades();
        especialidade.setNome("Infraestrutura");
        especialidade.setDivisao(divisao);
        especialidade.setDepartamento(departamento);
        entityManager.persist(especialidade);

        plantonista = new Funcionario();
        plantonista.setNome("Ana");
        plantonista.setMatricula("relatorio-" + java.util.UUID.randomUUID());
        entityManager.persist(plantonista);
        entityManager.flush();
    }

    @Test
    void gerar_comLimitesInclusivos_consideraRegistrosDoPeriodoEExcluiOsExternos() {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 20, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 10, 20, 18, 0);
        persistirChamado(inicio.minusSeconds(1));
        persistirChamado(inicio);
        persistirChamado(fim);
        persistirChamado(fim.plusSeconds(1));
        entityManager.flush();

        var resposta = relatorioService.gerar(inicio, fim);

        assertThat(resposta.totalAcionamentos()).isEqualTo(2);
        assertThat(resposta.porEspecialidade()).containsEntry("Infraestrutura", 2L);
        assertThat(resposta.porPlantonista()).containsEntry("Ana", 2L);
    }

    @Test
    void gerarSemanal_incluiChamadosDosUltimosSeteDiasEExcluiAnteriorAoPeriodo() {
        LocalDateTime momento = LocalDateTime.now();
        persistirChamado(momento.minusDays(3));
        persistirChamado(momento.minusDays(8));
        entityManager.flush();

        var resposta = relatorioService.gerarSemanal();

        assertThat(resposta.periodoInicio()).isEqualTo(resposta.periodoFim().minusDays(7));
        assertThat(resposta.totalAcionamentos()).isEqualTo(1);
        assertThat(resposta.porEspecialidade()).containsEntry("Infraestrutura", 1L);
    }

    @Test
    void gerarMensal_incluiChamadoDoMesAtualEExcluiChamadoDoMesAnterior() {
        LocalDateTime momento = LocalDateTime.now();
        persistirChamado(momento.minusMinutes(1));
        LocalDateTime primeiroDiaMes = YearMonth.from(momento).atDay(1).atStartOfDay();
        persistirChamado(primeiroDiaMes.minusSeconds(1));
        entityManager.flush();

        var resposta = relatorioService.gerarMensal();

        assertThat(resposta.periodoInicio()).isEqualTo(
                YearMonth.from(resposta.periodoFim()).atDay(1).atStartOfDay());
        assertThat(resposta.totalAcionamentos()).isEqualTo(1);
        assertThat(resposta.porPlantonista()).containsEntry("Ana", 1L);
    }

    private void persistirChamado(LocalDateTime dataHora) {
        Chamado chamado = new Chamado();
        chamado.setDataHoraAcionamento(dataHora);
        chamado.setEspecialidade(especialidade);
        chamado.setPlantonista(plantonista);
        chamado.setUsuarioResponsavel(plantonista);
        chamado.setMotivo("Teste de período do relatório");
        chamado.setStatus(StatusChamado.ABERTO);
        entityManager.persist(chamado);
    }
}
