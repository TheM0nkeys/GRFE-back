package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.EscalaRequest;
import br.com.itaipu.grfe.dto.response.EscalaResponse;
import br.com.itaipu.grfe.security.JwtAuthenticationFilter;
import br.com.itaipu.grfe.service.EscalaService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(EscalaController.class)
class EscalaControllerTest {

    private static final String ERRO_INTERVALO =
            "A data/hora de fim deve ser posterior à data/hora de início.";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private EscalaService escalaService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-20T07:59:59", "2026-10-20T08:00:00"})
    void criar_comFimAnteriorOuIgualRetorna400(String fim) throws Exception {
        org.mockito.Mockito.when(escalaService.criar(any(EscalaRequest.class)))
                .thenThrow(new IllegalArgumentException(ERRO_INTERVALO));

        mockMvc.perform(post("/escalas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyEscala(fim)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(ERRO_INTERVALO))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(escalaService).criar(any(EscalaRequest.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-20T07:59:59", "2026-10-20T08:00:00"})
    void atualizar_comFimAnteriorOuIgualRetorna400(String fim) throws Exception {
        org.mockito.Mockito.when(escalaService.atualizar(
                        org.mockito.ArgumentMatchers.eq(7L), any(EscalaRequest.class)))
                .thenThrow(new IllegalArgumentException(ERRO_INTERVALO));

        mockMvc.perform(put("/escalas/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyEscala(fim)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(ERRO_INTERVALO))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(escalaService).atualizar(org.mockito.ArgumentMatchers.eq(7L), any(EscalaRequest.class));
    }

    @Test
    void criar_comPeriodoValidoRetorna201ERespostaDaEscala() throws Exception {
        when(escalaService.criar(any(EscalaRequest.class))).thenReturn(
                new EscalaResponse(9L, LocalDateTime.of(2026, 10, 20, 8, 0),
                        LocalDateTime.of(2026, 10, 20, 18, 0), 10L, "Redes", 20L, "Ana"));

        mockMvc.perform(post("/escalas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyEscala("2026-10-20T18:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.especialidadeNome").value("Redes"))
                .andExpect(jsonPath("$.funcionarioNome").value("Ana"));

        verify(escalaService).criar(any(EscalaRequest.class));
    }

    private String bodyEscala(String fim) {
        return """
                {"dataHoraInicio":"2026-10-20T08:00:00","dataHoraFim":"%s",
                 "especialidadeId":10,"funcionarioId":20}
                """.formatted(fim);
    }
}
