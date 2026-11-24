package br.com.itaipu.grfe.client.dtoservicenow;

import java.util.List;

public record ServiceNowIncidentListaRaw(
        List<ServiceNowIncidentRaw.Result> result
) {
}
