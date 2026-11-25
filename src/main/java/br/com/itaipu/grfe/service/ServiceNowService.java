package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.client.ServiceNowClient;
import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.ServiceNowIncidentResponse;
import org.springframework.stereotype.Service;
import br.com.itaipu.grfe.client.dtoservicenow.ServiceNowIncidentListaRaw;
import br.com.itaipu.grfe.dto.response.ServiceNowConsultaResponse;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.com.itaipu.grfe.exception.ServiceNowIndisponivelException;

import java.util.regex.Pattern;

@Service
public class ServiceNowService {

    private static final Logger log =
            LoggerFactory.getLogger(ServiceNowService.class);

    private static final Pattern FORMATO_NUMERO =
            Pattern.compile("^INC\\d+$");

    private static final String CAMPOS =
            "sys_id,number,short_description,description,state,impact,urgency";

    private final ServiceNowClient serviceNowClient;

    public ServiceNowService(ServiceNowClient serviceNowClient) {
        this.serviceNowClient = serviceNowClient;
    }

    public ServiceNowIncidentResponse criarIncidente(
            ServiceNowIncidentRequest request) {

        try {
            return serviceNowClient.criarIncidente(request);

        } catch (Exception ex) {
            log.warn(
                    "Falha ao criar incidente no ServiceNow: {}",
                    ex.getMessage()
            );

            throw new ServiceNowIndisponivelException(
                    "Não foi possível criar o incidente no ServiceNow.",
                    ex
            );
        }
    }

    public ServiceNowIncidentResponse atualizarIncidente(
            String sysId,
            ServiceNowIncidentRequest request) {

        try {
            return serviceNowClient.atualizarIncidente(
                    sysId,
                    request
            );

        } catch (Exception ex) {
            log.warn(
                    "Falha ao atualizar incidente no ServiceNow: sysId={}, erro={}",
                    sysId,
                    ex.getMessage()
            );

            throw new ServiceNowIndisponivelException(
                    "Não foi possível atualizar o incidente no ServiceNow.",
                    ex
            );
        }
    }

    public ServiceNowConsultaResponse consultarPorNumero(String numero) {

        if (numero == null || !FORMATO_NUMERO.matcher(numero).matches()) {
            log.warn("Número de incidente em formato inválido: {}", numero);

            throw new IllegalArgumentException(
                    "Número de incidente inválido. Use o formato INC seguido de dígitos (ex.: INC0010001)."
            );
        }

        ServiceNowIncidentListaRaw raw;

        try {
            raw = serviceNowClient.buscarPorFiltro(
                    "number=" + numero,
                    CAMPOS,
                    1
            );
        } catch (Exception ex) {
            log.warn(
                    "ServiceNow indisponível ao consultar incidente {}: {}",
                    numero,
                    ex.getMessage()
            );

            return ServiceNowConsultaResponse.indisponivel();
        }

        if (raw == null ||
                raw.result() == null ||
                raw.result().isEmpty()) {

            log.warn(
                    "Incidente não encontrado no ServiceNow: {}",
                    numero
            );

            throw new EntidadeNaoEncontradaException(
                    "Incidente não encontrado no ServiceNow: " + numero
            );
        }

        log.info(
                "Incidente consultado no ServiceNow: {}",
                numero
        );

        return ServiceNowConsultaResponse.fromRaw(
                raw.result().get(0)
        );
    }
}