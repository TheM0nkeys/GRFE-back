package br.com.itaipu.grfe.repository;

import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.entity.Escala;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EscalaRepositoryIntegrationTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 20, 8, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 10, 20, 18, 0);

    @Autowired
    private EscalaRepository escalaRepository;
    @Autowired
    private EntityManager entityManager;

    private Especialidades especialidade;
    private Especialidades outraEspecialidade;
    private Funcionario funcionario;
    private Funcionario outroFuncionario;

    @BeforeEach
    void prepararDadosIsolados() {
        Divisao divisao = new Divisao();
        divisao.setNome("Divisão do teste");
        entityManager.persist(divisao);

        Departamento departamento = new Departamento();
        departamento.setNome("Departamento do teste");
        departamento.setDivisao(divisao);
        entityManager.persist(departamento);

        especialidade = novaEspecialidade("Redes", divisao, departamento);
        outraEspecialidade = novaEspecialidade("Banco", divisao, departamento);
        funcionario = novoFuncionario("Plantonista");
        outroFuncionario = novoFuncionario("Outro plantonista");

        Escala escala = new Escala();
        escala.setDataHoraInicio(INICIO);
        escala.setDataHoraFim(FIM);
        escala.setEspecialidade(especialidade);
        escala.setFuncionario(funcionario);
        entityManager.persist(escala);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void plantonistaEmEscala_noInstanteExatoDoInicio_consideraLimiteInclusivo() {
        assertThat(escalaRepository.plantonistaEmEscala(especialidade.getId(), funcionario.getId(), INICIO)).isTrue();
    }

    @Test
    void plantonistaEmEscala_noInstanteExatoDoFim_consideraLimiteInclusivo() {
        assertThat(escalaRepository.plantonistaEmEscala(especialidade.getId(), funcionario.getId(), FIM)).isTrue();
    }

    @Test
    void plantonistaEmEscala_antesDoInicio_retornaFalse() {
        assertThat(escalaRepository.plantonistaEmEscala(
                especialidade.getId(), funcionario.getId(), INICIO.minusNanos(1))).isFalse();
    }

    @Test
    void plantonistaEmEscala_depoisDoFim_retornaFalse() {
        assertThat(escalaRepository.plantonistaEmEscala(
                especialidade.getId(), funcionario.getId(), FIM.plusNanos(1))).isFalse();
    }

    @Test
    void plantonistaEmEscala_comEspecialidadeDiferente_retornaFalse() {
        assertThat(escalaRepository.plantonistaEmEscala(
                outraEspecialidade.getId(), funcionario.getId(), INICIO.plusHours(1))).isFalse();
    }

    @Test
    void plantonistaEmEscala_comFuncionarioDiferente_retornaFalse() {
        assertThat(escalaRepository.plantonistaEmEscala(
                especialidade.getId(), outroFuncionario.getId(), INICIO.plusHours(1))).isFalse();
    }

    @Test
    void plantonistaEmEscala_dentroDoIntervaloComAssociacoesCorretas_retornaTrue() {
        assertThat(escalaRepository.plantonistaEmEscala(
                especialidade.getId(), funcionario.getId(), INICIO.plusHours(1))).isTrue();
    }

    private Especialidades novaEspecialidade(String nome, Divisao divisao, Departamento departamento) {
        Especialidades nova = new Especialidades();
        nova.setNome(nome);
        nova.setDivisao(divisao);
        nova.setDepartamento(departamento);
        entityManager.persist(nova);
        return nova;
    }

    private Funcionario novoFuncionario(String nome) {
        Funcionario novo = new Funcionario();
        novo.setNome(nome);
        novo.setMatricula(nome + "-" + java.util.UUID.randomUUID());
        entityManager.persist(novo);
        return novo;
    }
}
