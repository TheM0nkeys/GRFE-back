package br.com.itaipu.grfe.client.dtoservicenow;

public record ServiceNowIncidentPayload(
        String short_description,
        String description,
        String impact,
        String urgency
) {
}
