package br.com.itaipu.grfe.dto;

import br.com.itaipu.grfe.client.dtoservicenow.ServiceNowIncidentPayload;
import br.com.itaipu.grfe.client.dtoservicenow.ServiceNowIncidentRaw;
import br.com.itaipu.grfe.dto.request.LoginRequest;
import br.com.itaipu.grfe.dto.request.ServiceNowIncidentRequest;
import br.com.itaipu.grfe.dto.response.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DtoCoverageTest {

    @Test void cobreRecordsSimplesEFactories() {
        LoginRequest login = new LoginRequest("admin", "senha");
        assertEquals("admin", login.login()); assertEquals("senha", login.senha());

        ServiceNowIncidentRequest req = new ServiceNowIncidentRequest("titulo","desc","1","2");
        assertEquals("titulo", req.short_description()); assertEquals("2", req.urgency());

        ServiceNowIncidentPayload payload = new ServiceNowIncidentPayload("titulo","desc","1","2");
        assertEquals("desc", payload.description());

        ServiceNowIncidentRaw.Result rawResult = new ServiceNowIncidentRaw.Result("sys","INC1","titulo","desc","1","2","3");
        ServiceNowIncidentRaw raw = new ServiceNowIncidentRaw(rawResult);
        assertEquals("sys", raw.result().sys_id()); assertEquals("INC1", raw.result().number());

        LoginResponse lr = new LoginResponse("token","admin","ROLE_ADMIN");
        assertEquals("token", lr.token());

        ServiceNowIncidentResponse.Result result = new ServiceNowIncidentResponse.Result("sys","INC1","titulo","desc","1","2","3");
        ServiceNowIncidentResponse response = new ServiceNowIncidentResponse(result);
        assertEquals("INC1", response.result().number());

        ErroResponse e1 = ErroResponse.of(400,"erro");
        ErroResponse e2 = ErroResponse.of(422,"erro", List.of("campo"));
        assertEquals(400,e1.status()); assertNull(e1.detalhes()); assertEquals(List.of("campo"),e2.detalhes());

        LocalDateTime inicio=LocalDateTime.of(2026,1,1,0,0), fim=inicio.plusDays(1);
        RelatorioAcionamentosResponse rel = new RelatorioAcionamentosResponse(inicio,fim,1,Map.of("A",1L),Map.of("B",1L),Map.of("C",1L),Map.of("D",1L));
        assertEquals(1,rel.totalAcionamentos()); assertEquals(1L,rel.porEspecialidade().get("A"));
    }
}
