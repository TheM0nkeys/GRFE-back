package br.com.itaipu.grfe.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ServiceNowIncidentRequest(
        @NotBlank(message = "A descrição resumida é obrigatória")
        String short_description,
        String description,
        @Pattern(regexp = "[1-3]", message = "O impacto deve ser 1, 2 ou 3")
        String impact,
        @Pattern(regexp = "[1-3]", message = "A urgência deve ser 1, 2 ou 3")
        String urgency
) {
}
