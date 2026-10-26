package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.response.RelatorioAcionamentosResponse;
import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import br.com.itaipu.grfe.specification.ChamadoSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        log.info("Gerando relatorio de acionamentos: periodoInicio={}, periodoFim={}", inicio, fim);

        var filtro = ChamadoSpecification.comFiltros(null, null, null, null, null, inicio, fim);
        List<Chamado> chamados = chamadoRepository.findAll(filtro);

        return new RelatorioAcionamentosResponse(
                inicio,
                fim,
                chamados.size(),
                agruparPor(chamados, c -> c.getEspecialidade().getNome()),
                agruparPor(chamados, c -> c.getPlantonista().getNome()),
                agruparPor(chamados, c -> c.getEspecialidade().getDivisao().getNome()),
                agruparPor(chamados, c -> c.getEspecialidade().getDepartamento().getNome())
        );
    }

    private Map<String, Long> agruparPor(List<Chamado> chamados, java.util.function.Function<Chamado, String> chave) {
        return chamados.stream()
                .collect(Collectors.groupingBy(chave, Collectors.counting()));
    }
}