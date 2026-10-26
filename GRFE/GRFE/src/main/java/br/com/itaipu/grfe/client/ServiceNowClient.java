package br.com.itaipu.grfe.client;

import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.ServiceNowIncidentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "serviceNowClient",
        url = "${servicenow.url}"
)
public interface ServiceNowClient {

    @PostMapping("/api/now/table/incident")
    ServiceNowIncidentResponse criarIncidente(
            @RequestBody ServiceNowIncidentRequest request
    );

    @PatchMapping("/api/now/table/incident/{sysId}")
    ServiceNowIncidentResponse atualizarIncidente(
            @PathVariable("sysId") String sysId,
            @RequestBody ServiceNowIncidentRequest request
    );
}