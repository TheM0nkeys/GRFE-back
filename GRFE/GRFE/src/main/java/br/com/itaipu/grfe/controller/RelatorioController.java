package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.response.RelatorioAcionamentosResponse;
import br.com.itaipu.grfe.service.RelatorioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "Relatórios", description = "Indicadores de acionamentos por especialidade, plantonista, divisão e departamento")
@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/semanal")
    public ResponseEntity<RelatorioAcionamentosResponse> semanal() {
        return ResponseEntity.ok(relatorioService.gerarSemanal());
    }

    @GetMapping("/mensal")
    public ResponseEntity<RelatorioAcionamentosResponse> mensal() {
        return ResponseEntity.ok(relatorioService.gerarMensal());
    }

    @GetMapping
    public ResponseEntity<RelatorioAcionamentosResponse> porPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim
    ) {
        return ResponseEntity.ok(relatorioService.gerar(dataInicio, dataFim));
    }
}