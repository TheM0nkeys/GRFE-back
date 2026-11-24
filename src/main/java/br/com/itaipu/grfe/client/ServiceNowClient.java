package br.com.itaipu.grfe.client;

import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.ServiceNowIncidentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import br.com.itaipu.grfe.client.dtoservicenow.ServiceNowIncidentListaRaw;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "serviceNowClient",
        url = "${servicenow.url}"
)
public interface ServiceNowClient {

    @PostMapping("/api/now/table/incident")
    ServiceNowIncidentResponse criarIncidente(
            @RequestBody ServiceNowIncidentRequest request
    );

    @GetMapping("/api/now/table/incident")
    ServiceNowIncidentListaRaw buscarPorFiltro(
            @RequestParam("sysparm_query") String query,
            @RequestParam("sysparm_fields") String campos,
            @RequestParam("sysparm_limit") int limite
    );

    @PatchMapping("/api/now/table/incident/{sysId}")
    ServiceNowIncidentResponse atualizarIncidente(
            @PathVariable("sysId") String sysId,
            @RequestBody ServiceNowIncidentRequest request
    );
}