package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.ChamadoRequest;
import br.com.itaipu.grfe.dto.response.ChamadoResponse;
import br.com.itaipu.grfe.dto.response.HistoricoAcionamentoResponse;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import br.com.itaipu.grfe.security.JwtAuthenticationFilter;
import br.com.itaipu.grfe.service.ChamadoService;
import br.com.itaipu.grfe.service.HistoricoAcionamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChamadoService chamadoService;

    @MockitoBean
    private HistoricoAcionamentoService historicoAcionamentoService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void listar_comQueryParams_encaminhaTodosOsFiltros() throws Exception {
        LocalDateTime inicio = LocalDateTime.of(2026, 10, 19, 8, 0);
        LocalDateTime fim = LocalDateTime.of(2026, 10, 19, 18, 0);
        when(chamadoService.listar(any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/chamados")
                        .param("status", "ABERTO")
                        .param("especialidadeId", "11")
                        .param("plantonistaId", "22")
                        .param("divisaoId", "33")
                        .param("departamentoId", "44")
                        .param("dataInicio", "2026-10-19T08:00:00")
                        .param("dataFim", "2026-10-19T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(chamadoService).listar(StatusChamado.ABERTO, 11L, 22L, 33L, 44L, inicio, fim);
    }

    @Test
    void atualizarStatus_comStatusValido_retornaChamadoAtualizado() throws Exception {
        when(chamadoService.atualizarStatus(7L, StatusChamado.FECHADO)).thenReturn(respostaChamado(StatusChamado.FECHADO));

        mockMvc.perform(patch("/chamados/7/status").param("status", "FECHADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("FECHADO"));

        verify(chamadoService).atualizarStatus(7L, StatusChamado.FECHADO);
    }

    @Test
    void listarHistorico_retornaEventosDoServico() throws Exception {
        LocalDateTime dataHora = LocalDateTime.of(2026, 10, 19, 9, 30);
        when(historicoAcionamentoService.listarPorChamado(7L)).thenReturn(
                List.of(new HistoricoAcionamentoResponse(3L, dataHora, 2L, "Ana", "Em análise")));

        mockMvc.perform(get("/chamados/7/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].autorNome").value("Ana"))
                .andExpect(jsonPath("$[0].comentario").value("Em análise"));

        verify(historicoAcionamentoService).listarPorChamado(7L);
    }

    @Test
    void criar_comBodyValido_retorna201() throws Exception {
        when(chamadoService.criar(any(ChamadoRequest.class))).thenReturn(respostaChamado(StatusChamado.ABERTO));

        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyChamadoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ABERTO"));

        verify(chamadoService).criar(any(ChamadoRequest.class));
    }

    @Test
    void criar_comBodyInvalido_retorna400SemChamarServico() throws Exception {
        mockMvc.perform(post("/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(6)));

        verify(chamadoService, never()).criar(any(ChamadoRequest.class));
    }

    @Test
    void registrarHistorico_comBodyInvalido_retorna400SemChamarServico() throws Exception {
        mockMvc.perform(post("/chamados/7/historico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comentario\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(3)));

        verify(historicoAcionamentoService, never()).registrar(eq(7L), any());
    }

    private ChamadoResponse respostaChamado(StatusChamado status) {
        return new ChamadoResponse(7L, LocalDateTime.of(2026, 10, 19, 8, 0), 11L, "Redes",
                22L, "Plantonista", 33L, "Responsável", "Falha de rede", "INC-123", status);
    }

    private String bodyChamadoValido() {
        return """
                {"dataHoraAcionamento":"2026-10-19T08:00:00","especialidadeId":11,
                 "plantonistaId":22,"usuarioResponsavelId":33,"motivo":"Falha de rede",
                 "numeroIncidente":"INC-123","status":"ABERTO"}
                """;
    }
}