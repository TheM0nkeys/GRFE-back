package br.com.itaipu.grfe.client.dtoservicenow;

public record ServiceNowIncidentRaw(
        Result result
) {
    public record Result(
            String sys_id,
            String number,
            String short_description,
            String description,
            String impact,
            String urgency,
            String state
    ) {
    }
}
