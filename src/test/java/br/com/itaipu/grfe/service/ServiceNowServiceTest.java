package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.client.ServiceNowClient;
import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.ServiceNowIncidentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceNowServiceTest {

    @Mock
    private ServiceNowClient serviceNowClient;

    private ServiceNowService service;

    @BeforeEach
    void setUp() {
        service = new ServiceNowService(serviceNowClient);
    }

    @Test
    void criarIncidente_delegaChamadaAoClient() {
        ServiceNowIncidentRequest request = request();
        ServiceNowIncidentResponse response = response("SYS001", "INC001");
        when(serviceNowClient.criarIncidente(request)).thenReturn(response);

        ServiceNowIncidentResponse resultado = service.criarIncidente(request);

        assertSame(response, resultado);
        verify(serviceNowClient).criarIncidente(request);
    }

    @Test
    void atualizarIncidente_delegaChamadaAoClientComSysId() {
        ServiceNowIncidentRequest request = request();
        ServiceNowIncidentResponse response = response("SYS001", "INC001");
        when(serviceNowClient.atualizarIncidente("SYS001", request)).thenReturn(response);

        ServiceNowIncidentResponse resultado = service.atualizarIncidente("SYS001", request);

        assertSame(response, resultado);
        verify(serviceNowClient).atualizarIncidente("SYS001", request);
    }

    private ServiceNowIncidentRequest request() {
        return new ServiceNowIncidentRequest(
                "Falha no sistema", "Sistema indisponível", "2", "2"
        );
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
