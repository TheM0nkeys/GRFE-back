package br.com.itaipu.grfe.dto.response;

public record ServiceNowIncidentResponse(
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