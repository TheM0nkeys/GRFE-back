package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import br.com.itaipu.grfe.exception.GlobalExceptionHandler;
import br.com.itaipu.grfe.specification.ChamadoSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChamadoRepositoryIntegrationTest {

    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private EntityManager entityManager;

    private final LocalDateTime inicio = LocalDateTime.of(2026, 10, 19, 8, 0);
    private final LocalDateTime fim = LocalDateTime.of(2026, 10, 19, 18, 0);

    @Test
    void specification_aplicaFiltrosIsoladosECombinadosIncluindoDivisaoEDepartamento() {
        Divisao divisaoUm = persistir(new Divisao(), divisao -> divisao.setNome("Divisão A"));
        Divisao divisaoDois = persistir(new Divisao(), divisao -> divisao.setNome("Divisão B"));
        Departamento departamentoUm = departamento("Departamento A", divisaoUm);
        Departamento departamentoDois = departamento("Departamento B", divisaoUm);
        Departamento departamentoTres = departamento("Departamento C", divisaoDois);
        Especialidades especialidadeUm = especialidade("Redes", divisaoUm, departamentoUm);
        Especialidades especialidadeDois = especialidade("Banco", divisaoUm, departamentoDois);
        Especialidades especialidadeTres = especialidade("Sistemas", divisaoDois, departamentoTres);
        Funcionario plantonistaUm = funcionario("Plantonista A");
        Funcionario plantonistaDois = funcionario("Plantonista B");

        Chamado chamadoUm = chamado(especialidadeUm, plantonistaUm, inicio.plusHours(1), StatusChamado.ABERTO);
        Chamado chamadoDois = chamado(especialidadeDois, plantonistaUm, inicio.plusHours(2), StatusChamado.FECHADO);
        Chamado chamadoTres = chamado(especialidadeTres, plantonistaDois, inicio.minusHours(1), StatusChamado.ABERTO);
        entityManager.flush();

        assertThat(buscar(StatusChamado.ABERTO, null, null, null, null, null, null))
                .containsExactlyInAnyOrder(chamadoUm, chamadoTres);
        assertThat(buscar(null, especialidadeDois.getId(), null, null, null, null, null))
                .containsExactly(chamadoDois);
        assertThat(buscar(null, null, plantonistaUm.getId(), null, null, null, null))
                .containsExactlyInAnyOrder(chamadoUm, chamadoDois);
        assertThat(buscar(null, null, null, divisaoUm.getId(), null, null, null))
                .containsExactlyInAnyOrder(chamadoUm, chamadoDois);
        assertThat(buscar(null, null, null, null, departamentoDois.getId(), null, null))
                .containsExactly(chamadoDois);
        assertThat(buscar(null, null, null, null, null, inicio, fim))
                .containsExactlyInAnyOrder(chamadoUm, chamadoDois);
        assertThat(buscar(StatusChamado.ABERTO, especialidadeUm.getId(), plantonistaUm.getId(),
                divisaoUm.getId(), departamentoUm.getId(), inicio, fim))
                .containsExactly(chamadoUm);
    }

    @Test
    void numeroIncidenteDuplicado_disparaViolacaoTratadaComoConflict() {
        String numeroIncidente = "IT-" + UUID.randomUUID();
        Chamado original = chamadoMinimo(numeroIncidente);
        chamadoRepository.saveAndFlush(original);

        Chamado duplicado = chamadoMinimo(numeroIncidente);
        DataIntegrityViolationException violacao = assertThrows(DataIntegrityViolationException.class,
                () -> chamadoRepository.saveAndFlush(duplicado));

        var resposta = new GlobalExceptionHandler().handleDataIntegrityViolation(violacao);
        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(resposta.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    private List<Chamado> buscar(StatusChamado status,
                                 Long especialidadeId,
                                 Long plantonistaId,
                                 Long divisaoId,
                                 Long departamentoId,
                                 LocalDateTime dataInicio,
                                 LocalDateTime dataFim) {
        return chamadoRepository.findAll(ChamadoSpecification.comFiltros(status, especialidadeId, plantonistaId,
                divisaoId, departamentoId, dataInicio, dataFim));
    }

    private Departamento departamento(String nome, Divisao divisao) {
        Departamento departamento = new Departamento();
        departamento.setNome(nome);
        departamento.setDivisao(divisao);
        entityManager.persist(departamento);
        return departamento;
    }

    private Especialidades especialidade(String nome, Divisao divisao, Departamento departamento) {
        Especialidades especialidade = new Especialidades();
        especialidade.setNome(nome);
        especialidade.setDivisao(divisao);
        especialidade.setDepartamento(departamento);
        entityManager.persist(especialidade);
        return especialidade;
    }

    private Funcionario funcionario(String nome) {
        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome);
        funcionario.setMatricula(UUID.randomUUID().toString());
        entityManager.persist(funcionario);
        return funcionario;
    }

    private Chamado chamado(Especialidades especialidade,
                            Funcionario plantonista,
                            LocalDateTime dataHora,
                            StatusChamado status) {
        Chamado chamado = new Chamado();
        chamado.setDataHoraAcionamento(dataHora);
        chamado.setEspecialidade(especialidade);
        chamado.setPlantonista(plantonista);
        chamado.setUsuarioResponsavel(plantonista);
        chamado.setMotivo("Teste de specification");
        chamado.setNumeroIncidente("SPEC-" + UUID.randomUUID());
        chamado.setStatus(status);
        entityManager.persist(chamado);
        return chamado;
    }

    private Chamado chamadoMinimo(String numeroIncidente) {
        Divisao divisao = new Divisao();
        divisao.setNome("Divisão " + UUID.randomUUID());
        entityManager.persist(divisao);

        Departamento departamento = new Departamento();
        departamento.setNome("Departamento " + UUID.randomUUID());
        departamento.setDivisao(divisao);
        entityManager.persist(departamento);

        Especialidades especialidade = new Especialidades();
        especialidade.setNome("Especialidade " + UUID.randomUUID());
        especialidade.setDivisao(divisao);
        especialidade.setDepartamento(departamento);
        entityManager.persist(especialidade);

        Funcionario funcionario = funcionario("Funcionário " + UUID.randomUUID());

        Chamado chamado = new Chamado();
        chamado.setDataHoraAcionamento(inicio);
        chamado.setEspecialidade(especialidade);
        chamado.setPlantonista(funcionario);
        chamado.setUsuarioResponsavel(funcionario);
        chamado.setMotivo("Teste de índice único");
        chamado.setNumeroIncidente(numeroIncidente);
        chamado.setStatus(StatusChamado.ABERTO);
        return chamado;
    }

    private <T> T persistir(T entidade, java.util.function.Consumer<T> configurar) {
        configurar.accept(entidade);
        entityManager.persist(entidade);
        return entidade;
    }
}