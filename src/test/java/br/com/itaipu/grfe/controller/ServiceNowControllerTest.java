package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.client.ServiceNowClient;
import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.ServiceNowIncidentResponse;
import br.com.itaipu.grfe.service.ServiceNowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ServiceNowController.class, ServiceNowService.class})
class ServiceNowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceNowClient serviceNowClient;

    @Test
    void criarIncidente_comClientMockado_retorna201() throws Exception {
        when(serviceNowClient.criarIncidente(any(ServiceNowIncidentRequest.class)))
                .thenReturn(response("SYS001", "INC001"));

        mockMvc.perform(post("/integracao/servicenow/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result.sys_id").value("SYS001"))
                .andExpect(jsonPath("$.result.number").value("INC001"));
    }

    @Test
    void atualizarIncidente_comClientMockado_retorna200() throws Exception {
        when(serviceNowClient.atualizarIncidente(
                org.mockito.ArgumentMatchers.eq("SYS001"),
                any(ServiceNowIncidentRequest.class)
        )).thenReturn(response("SYS001", "INC001"));

        mockMvc.perform(patch("/integracao/servicenow/incidentes/SYS001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.sys_id").value("SYS001"));
    }

    @Test
    void criarIncidente_quandoClientFalha_retorna503SemChamarServiceNowReal() throws Exception {
        when(serviceNowClient.criarIncidente(any(ServiceNowIncidentRequest.class)))
                .thenThrow(new RuntimeException("ServiceNow indisponível"));

        mockMvc.perform(post("/integracao/servicenow/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest()))
                .andExpect(status().isServiceUnavailable());
    }

    private String jsonRequest() {
        return """
                {
                  "short_description": "Falha no sistema",
                  "description": "Sistema indisponível",
                  "impact": "2",
                  "urgency": "2"
                }
                """;
    }

    private ServiceNowIncidentResponse response(String sysId, String number) {
        return new ServiceNowIncidentResponse(
                new ServiceNowIncidentResponse.Result(
                        sysId,
                        number,
                        "Falha no sistema",
                        "Sistema indisponível",
                        "2",
                        "2",
                        "1"
                )
        );
    }
}
