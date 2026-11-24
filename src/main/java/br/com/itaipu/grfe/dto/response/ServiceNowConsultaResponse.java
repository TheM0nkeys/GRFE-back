package br.com.itaipu.grfe.dto.response;

import br.com.itaipu.grfe.client.dtoservicenow.ServiceNowIncidentRaw;

public record ServiceNowConsultaResponse(
        boolean disponivel,
        String sysId,
        String numero,
        String descricaoResumida,
        String descricao,
        String estado,
        String impacto,
        String urgencia
) {

    public static ServiceNowConsultaResponse fromRaw(ServiceNowIncidentRaw.Result r) {
        return new ServiceNowConsultaResponse(
                true,
                r.sys_id(),
                r.number(),
                r.short_description(),
                r.description(),
                descreverEstado(r.state()),
                r.impact(),
                r.urgency()
        );
    }

    public static ServiceNowConsultaResponse indisponivel() {
        return new ServiceNowConsultaResponse(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static String descreverEstado(String codigo) {
        if (codigo == null) {
            return null;
        }

        return switch (codigo) {
            case "1" -> "Novo";
            case "2" -> "Em andamento";
            case "3" -> "Em espera";
            case "6" -> "Resolvido";
            case "7" -> "Fechado";
            case "8" -> "Cancelado";
            default -> codigo;
        };
    }
}