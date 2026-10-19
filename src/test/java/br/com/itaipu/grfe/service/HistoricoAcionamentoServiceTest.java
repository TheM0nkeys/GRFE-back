package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.response.HistoricoAcionamentoResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoricoAcionamentoServiceTest {

    @Mock
    private HistoricoAcionamentoRepository historicoAcionamentoRepository;
    @Mock
    private ChamadoRepository chamadoRepository;
    @Mock
    private FuncionarioRepository funcionarioRepository;
    @InjectMocks
    private HistoricoAcionamentoService historicoAcionamentoService;

    @Test
    void listarPorChamado_ordenaPorDataEIncluiAutorCorrespondente() {
        Funcionario autora = funcionario(1L, "Ana");
        Funcionario autorb = funcionario(2L, "Bruno");
        HistoricoAcionamento maisRecente = historico(20L, LocalDateTime.of(2026, 10, 19, 11, 0), autorb, "Resolvido");
        HistoricoAcionamento maisAntigo = historico(10L, LocalDateTime.of(2026, 10, 19, 9, 0), autora, "Em análise");
        Chamado chamado = new Chamado();
        chamado.setHistorico(new HashSet<>(Set.of(maisRecente, maisAntigo)));
        when(chamadoRepository.findById(5L)).thenReturn(Optional.of(chamado));

        List<HistoricoAcionamentoResponse> resposta = historicoAcionamentoService.listarPorChamado(5L);

        assertThat(resposta).extracting(HistoricoAcionamentoResponse::dataHora)
                .containsExactly(maisAntigo.getDataHora(), maisRecente.getDataHora());
        assertThat(resposta).extracting(HistoricoAcionamentoResponse::autorNome)
                .containsExactly("Ana", "Bruno");
    }

    @Test
    void listarPorChamado_comChamadoInexistente_lancaNaoEncontrado() {
        when(chamadoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historicoAcionamentoService.listarPorChamado(404L))
                .isInstanceOf(EntidadeNaoEncontradaException.class)
                .hasMessage("Acionamento não encontrado: 404");
    }

    private HistoricoAcionamento historico(Long id, LocalDateTime dataHora, Funcionario autor, String comentario) {
        HistoricoAcionamento historico = new HistoricoAcionamento();
        historico.setId(id);
        historico.setDataHora(dataHora);
        historico.setAutor(autor);
        historico.setComentario(comentario);
        return historico;
    }

    private Funcionario funcionario(Long id, String nome) {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(id);
        funcionario.setNome(nome);
        return funcionario;
    }
}