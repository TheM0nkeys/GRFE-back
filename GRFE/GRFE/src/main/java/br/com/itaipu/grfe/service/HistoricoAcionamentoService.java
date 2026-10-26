package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.HistoricoAcionamentoRequest;
import br.com.itaipu.grfe.dto.response.HistoricoAcionamentoResponse;
import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.HistoricoAcionamento;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import br.com.itaipu.grfe.repository.HistoricoAcionamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class HistoricoAcionamentoService {

    private static final Logger log = LoggerFactory.getLogger(HistoricoAcionamentoService.class);

    private final HistoricoAcionamentoRepository historicoAcionamentoRepository;
    private final ChamadoRepository chamadoRepository;
    private final FuncionarioRepository funcionarioRepository;

    public HistoricoAcionamentoService(HistoricoAcionamentoRepository historicoAcionamentoRepository,
                                       ChamadoRepository chamadoRepository,
                                       FuncionarioRepository funcionarioRepository) {
        this.historicoAcionamentoRepository = historicoAcionamentoRepository;
        this.chamadoRepository = chamadoRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    public List<HistoricoAcionamentoResponse> listarPorChamado(Long chamadoId) {
        Chamado chamado = buscarChamado(chamadoId);
        return chamado.getHistorico()
                .stream()
                .sorted(Comparator.comparing(HistoricoAcionamento::getDataHora))
                .map(HistoricoAcionamentoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public HistoricoAcionamentoResponse registrar(Long chamadoId, HistoricoAcionamentoRequest request) {
        Chamado chamado = buscarChamado(chamadoId);

        Funcionario autor = funcionarioRepository.findById(request.autorId())
                .orElseThrow(() -> {
                    log.warn("Autor não encontrado ao registrar histórico: autorId={}", request.autorId());
                    return new EntidadeNaoEncontradaException("Autor não encontrado: " + request.autorId());
                });

        HistoricoAcionamento historico = request.toEntity();
        historico.setAutor(autor);
        historico.setChamado(chamado);

        HistoricoAcionamento salvo = historicoAcionamentoRepository.save(historico);
        chamado.getHistorico().add(salvo);

        log.info("Histórico registrado: chamadoId={}, autorId={}", chamadoId, request.autorId());

        return HistoricoAcionamentoResponse.fromEntity(salvo);
    }

    private Chamado buscarChamado(Long chamadoId) {
        return chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> {
                    log.warn("Acionamento não encontrado ao acessar histórico: chamadoId={}", chamadoId);
                    return new EntidadeNaoEncontradaException("Acionamento não encontrado: " + chamadoId);
                });
    }
}