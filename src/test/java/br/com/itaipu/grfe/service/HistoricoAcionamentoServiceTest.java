package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.response.HistoricoAcionamentoResponse;
import br.com.itaipu.grfe.dto.request.HistoricoAcionamentoRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    @Test
    void registrar_comRelacoesExistentes_salvaEAssociaHistorico() {
        Chamado chamado = chamado(5L);
        Funcionario autor = funcionario(7L, "Ana");
        HistoricoAcionamentoRequest request = new HistoricoAcionamentoRequest(
                LocalDateTime.of(2026, 1, 1, 12, 0), 7L, "ok");
        when(chamadoRepository.findById(5L)).thenReturn(Optional.of(chamado));
        when(funcionarioRepository.findById(7L)).thenReturn(Optional.of(autor));
        when(historicoAcionamentoRepository.save(any())).thenAnswer(invocation -> {
            HistoricoAcionamento historico = invocation.getArgument(0);
            historico.setId(9L);
            return historico;
        });

        var resposta = historicoAcionamentoService.registrar(5L, request);

        assertThat(resposta.id()).isEqualTo(9L);
        assertThat(resposta.autorNome()).isEqualTo("Ana");
        assertThat(chamado.getHistorico()).hasSize(1);
        verify(historicoAcionamentoRepository).save(any());
    }

    @Test
    void registrar_comChamadoInexistente_naoBuscaAutor() {
        when(chamadoRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historicoAcionamentoService.registrar(5L,
                new HistoricoAcionamentoRequest(LocalDateTime.now(), 7L, "ok")))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
        verify(funcionarioRepository, never()).findById(any());
    }

    @Test
    void registrar_comAutorInexistente_naoSalvaHistorico() {
        when(chamadoRepository.findById(5L)).thenReturn(Optional.of(chamado(5L)));
        when(funcionarioRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historicoAcionamentoService.registrar(5L,
                new HistoricoAcionamentoRequest(LocalDateTime.now(), 7L, "ok")))
                .isInstanceOf(EntidadeNaoEncontradaException.class);
        verify(historicoAcionamentoRepository, never()).save(any());
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

    private Chamado chamado(Long id) {
        Chamado chamado = new Chamado();
        chamado.setId(id);
        chamado.setHistorico(new HashSet<>());
        return chamado;
    }
}