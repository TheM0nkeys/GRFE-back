package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.client.ServiceNowClient;
import br.com.itaipu.grfe.client.dtoservicenow.ServiceNowIncidentListaRaw;
import br.com.itaipu.grfe.client.dtoservicenow.ServiceNowIncidentRaw;
import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.ServiceNowConsultaResponse;
import br.com.itaipu.grfe.dto.response.ServiceNowIncidentResponse;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import br.com.itaipu.grfe.exception.ServiceNowIndisponivelException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        when(serviceNowClient.criarIncidente(request))
                .thenReturn(response);

        ServiceNowIncidentResponse resultado =
                service.criarIncidente(request);

        assertSame(response, resultado);
        verify(serviceNowClient).criarIncidente(request);
    }

    @Test
    void criarIncidente_quandoServiceNowFalha_lancaServiceNowIndisponivelException() {
        ServiceNowIncidentRequest request = request();

        when(serviceNowClient.criarIncidente(request))
                .thenThrow(new RuntimeException("Falha externa"));

        ServiceNowIndisponivelException exception =
                assertThrows(
                        ServiceNowIndisponivelException.class,
                        () -> service.criarIncidente(request)
                );

        assertEquals(
                "Não foi possível criar o incidente no ServiceNow.",
                exception.getMessage()
        );

        verify(serviceNowClient).criarIncidente(request);
    }

    @Test
    void atualizarIncidente_quandoServiceNowFalha_lancaServiceNowIndisponivelException() {
        ServiceNowIncidentRequest request = request();

        when(serviceNowClient.atualizarIncidente("SYS001", request))
                .thenThrow(new RuntimeException("Falha externa"));

        ServiceNowIndisponivelException exception =
                assertThrows(
                        ServiceNowIndisponivelException.class,
                        () -> service.atualizarIncidente("SYS001", request)
                );

        assertEquals(
                "Não foi possível atualizar o incidente no ServiceNow.",
                exception.getMessage()
        );

        verify(serviceNowClient)
                .atualizarIncidente("SYS001", request);
    }

    @Test
    void atualizarIncidente_delegaChamadaAoClientComSysId() {
        ServiceNowIncidentRequest request = request();
        ServiceNowIncidentResponse response = response("SYS001", "INC001");

        when(serviceNowClient.atualizarIncidente("SYS001", request))
                .thenReturn(response);

        ServiceNowIncidentResponse resultado =
                service.atualizarIncidente("SYS001", request);

        assertSame(response, resultado);

        verify(serviceNowClient)
                .atualizarIncidente("SYS001", request);
    }

    @Test
    void consultarPorNumero_quandoEncontrado_retornaIncidente() {
        ServiceNowIncidentRaw.Result raw =
                new ServiceNowIncidentRaw.Result(
                        "SYS001",
                        "INC001",
                        "Falha no sistema",
                        "Sistema indisponível",
                        "2",
                        "2",
                        "1"
                );

        ServiceNowIncidentListaRaw resposta =
                new ServiceNowIncidentListaRaw(List.of(raw));

        when(serviceNowClient.buscarPorFiltro(
                eq("number=INC001"),
                anyString(),
                eq(1)
        )).thenReturn(resposta);

        ServiceNowConsultaResponse resultado =
                service.consultarPorNumero("INC001");

        assertTrue(resultado.disponivel());
        assertEquals("SYS001", resultado.sysId());
        assertEquals("INC001", resultado.numero());
        assertEquals("Falha no sistema", resultado.descricaoResumida());
        assertEquals("Sistema indisponível", resultado.descricao());
        assertEquals("Novo", resultado.estado());
        assertEquals("2", resultado.impacto());
        assertEquals("2", resultado.urgencia());

        verify(serviceNowClient).buscarPorFiltro(
                eq("number=INC001"),
                anyString(),
                eq(1)
        );
    }

    @Test
    void consultarPorNumero_quandoFormatoInvalido_lancaExcecao() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.consultarPorNumero("abc")
                );

        assertTrue(
                exception.getMessage().contains(
                        "Número de incidente inválido"
                )
        );

        verifyNoInteractions(serviceNowClient);
    }

    @Test
    void consultarPorNumero_quandoNaoEncontrado_lancaExcecao() {
        ServiceNowIncidentListaRaw resposta =
                new ServiceNowIncidentListaRaw(List.of());

        when(serviceNowClient.buscarPorFiltro(
                eq("number=INC9999999"),
                anyString(),
                eq(1)
        )).thenReturn(resposta);

        EntidadeNaoEncontradaException exception =
                assertThrows(
                        EntidadeNaoEncontradaException.class,
                        () -> service.consultarPorNumero("INC9999999")
                );

        assertTrue(
                exception.getMessage().contains(
                        "Incidente não encontrado no ServiceNow"
                )
        );
    }

    @Test
    void consultarPorNumero_quandoServiceNowIndisponivel_retornaDisponivelFalse() {
        when(serviceNowClient.buscarPorFiltro(
                eq("number=INC001"),
                anyString(),
                eq(1)
        )).thenThrow(
                new RuntimeException("ServiceNow indisponível")
        );

        ServiceNowConsultaResponse resultado =
                service.consultarPorNumero("INC001");

        assertFalse(resultado.disponivel());
        assertNull(resultado.sysId());
        assertNull(resultado.numero());
    }

    private ServiceNowIncidentRequest request() {
        return new ServiceNowIncidentRequest(
                "Falha no sistema",
                "Sistema indisponível",
                "2",
                "2"
        );
    }

    private ServiceNowIncidentResponse response(
            String sysId,
            String number
    ) {
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