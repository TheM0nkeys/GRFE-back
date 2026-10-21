package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.response.RelatorioAcionamentosResponse;
import br.com.itaipu.grfe.security.JwtAuthenticationFilter;
import br.com.itaipu.grfe.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(RelatorioController.class)
class RelatorioControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RelatorioService relatorioService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void semanal_retornaEstruturaDeRelatorioMesmoSemRegistros() throws Exception {
        RelatorioAcionamentosResponse resposta = relatorioVazio();
        when(relatorioService.gerarSemanal()).thenReturn(resposta);

        mockMvc.perform(get("/relatorios/semanal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodoInicio").value("2026-10-13T08:00:00"))
                .andExpect(jsonPath("$.periodoFim").value("2026-10-20T08:00:00"))
                .andExpect(jsonPath("$.totalAcionamentos").value(0))
                .andExpect(jsonPath("$.porEspecialidade").isEmpty())
                .andExpect(jsonPath("$.porPlantonista").isEmpty())
                .andExpect(jsonPath("$.porDivisao").isEmpty())
                .andExpect(jsonPath("$.porDepartamento").isEmpty());

        verify(relatorioService).gerarSemanal();
    }

    @Test
    void mensal_retornaStatusOkEEncaminhaAoServico() throws Exception {
        when(relatorioService.gerarMensal()).thenReturn(relatorioVazio());

        mockMvc.perform(get("/relatorios/mensal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAcionamentos").value(0))
                .andExpect(jsonPath("$.porEspecialidade").isEmpty());

        verify(relatorioService).gerarMensal();
    }

    @Test
    void porPeriodo_repassaDatasIsoEDevolveCamposDoRelatorio() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 1, 0, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 10, 20, 18, 30);
        when(relatorioService.gerar(inicio, fim)).thenReturn(new RelatorioAcionamentosResponse(
                inicio, fim, 2L, Map.of("Redes", 2L), Map.of("Ana", 2L),
                Map.of("Operações", 2L), Map.of("Telecom", 2L)));

        mockMvc.perform(get("/relatorios")
                        .param("dataInicio", "2026-10-01T00:00:00")
                        .param("dataFim", "2026-10-20T18:30:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodoInicio").value("2026-10-01T00:00:00"))
                .andExpect(jsonPath("$.periodoFim").value("2026-10-20T18:30:00"))
                .andExpect(jsonPath("$.totalAcionamentos").value(2))
                .andExpect(jsonPath("$.porEspecialidade.Redes").value(2))
                .andExpect(jsonPath("$.porPlantonista.Ana").value(2))
                .andExpect(jsonPath("$.porDivisao.Operações").value(2))
                .andExpect(jsonPath("$.porDepartamento.Telecom").value(2));

        verify(relatorioService).gerar(inicio, fim);
    }

    @Test
    void porPeriodo_comParametroDeDataInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/relatorios")
                        .param("dataInicio", "data-invalida")
                        .param("dataFim", "2026-10-20T18:30:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("dataInicio")));
    }

    @Test
    void porPeriodo_semParametroObrigatorio_retorna500ConformeHandlerAtual() throws Exception {
        mockMvc.perform(get("/relatorios").param("dataInicio", "2026-10-01T00:00:00"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.mensagem").value("Erro interno inesperado"));
    }

    private RelatorioAcionamentosResponse relatorioVazio() {
        return new RelatorioAcionamentosResponse(
                LocalDateTime.of(2026, 10, 13, 8, 0),
                LocalDateTime.of(2026, 10, 20, 8, 0),
                0L, Map.of(), Map.of(), Map.of(), Map.of());
    }
}
