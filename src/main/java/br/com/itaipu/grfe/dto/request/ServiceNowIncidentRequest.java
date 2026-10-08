package br.com.itaipu.grfe.dto.request;

public record ServiceNowIncidentRequest(
        String short_description,
        String description,
        String impact,
        String urgency
) {
}