package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.response.RelatorioAcionamentosResponse;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import br.com.itaipu.grfe.repository.projecao.ContagemPorNome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private static final Logger log = LoggerFactory.getLogger(RelatorioService.class);

    private final ChamadoRepository chamadoRepository;

    public RelatorioService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    public RelatorioAcionamentosResponse gerarSemanal() {
        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = fim.minusDays(7);
        return gerar(inicio, fim);
    }

    public RelatorioAcionamentosResponse gerarMensal() {
        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = YearMonth.from(LocalDate.now()).atDay(1).atStartOfDay();
        return gerar(inicio, fim);
    }

    public RelatorioAcionamentosResponse gerar(LocalDateTime inicio, LocalDateTime fim) {
        if (fim.isBefore(inicio)) {                                                  // NOVO
            log.warn("Período de relatório invertido: inicio={}, fim={}", inicio, fim);
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        log.info("Gerando relatorio de acionamentos: periodoInicio={}, periodoFim={}", inicio, fim);

        return new RelatorioAcionamentosResponse(
                inicio,
                fim,
                chamadoRepository.contarNoPeriodo(inicio, fim),
                paraMapa(chamadoRepository.contarPorEspecialidade(inicio, fim)),
                paraMapa(chamadoRepository.contarPorPlantonista(inicio, fim)),
                paraMapa(chamadoRepository.contarPorDivisao(inicio, fim)),
                paraMapa(chamadoRepository.contarPorDepartamento(inicio, fim))
        );
    }

    private Map<String, Long> paraMapa(List<ContagemPorNome> contagens) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        contagens.forEach(contagem -> mapa.put(contagem.nome(), contagem.total()));
        return mapa;
    }
}