package br.com.itaipu.grfe.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record RelatorioAcionamentosResponse(
        LocalDateTime periodoInicio,
        LocalDateTime periodoFim,
        long totalAcionamentos,
        Map<String, Long> porEspecialidade,
        Map<String, Long> porPlantonista,
        Map<String, Long> porDivisao,
        Map<String, Long> porDepartamento
) {
}