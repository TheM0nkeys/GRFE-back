package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.client.ServiceNowClient;
import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.ServiceNowIncidentResponse;
import org.springframework.stereotype.Service;

@Service
public class ServiceNowService {

    private final ServiceNowClient serviceNowClient;

    public ServiceNowService(ServiceNowClient serviceNowClient) {
        this.serviceNowClient = serviceNowClient;
    }

    public ServiceNowIncidentResponse criarIncidente(
            ServiceNowIncidentRequest request) {

        return serviceNowClient.criarIncidente(request);
    }

    public ServiceNowIncidentResponse atualizarIncidente(
            String sysId,
            ServiceNowIncidentRequest request) {

        return serviceNowClient.atualizarIncidente(sysId, request);
    }
}