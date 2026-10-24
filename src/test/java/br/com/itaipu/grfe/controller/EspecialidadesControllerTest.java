package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.EspecialidadesRequest;
import br.com.itaipu.grfe.exception.GlobalExceptionHandler;
import br.com.itaipu.grfe.service.EspecialidadesService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {EspecialidadesController.class, GlobalExceptionHandler.class})
class EspecialidadesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EspecialidadesService especialidadesService;

    @Test
    void criar_quandoDepartamentoNaoPertenceADivisao_retorna400() throws Exception {
        when(especialidadesService.criar(any(EspecialidadesRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "O departamento informado (Financeiro) não pertence à divisão informada (TI)"
                ));

        mockMvc.perform(post("/especialidades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Redes",
                                  "descricao": "Suporte de redes",
                                  "divisaoId": 1,
                                  "departamentoId": 10
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value(
                        "O departamento informado (Financeiro) não pertence à divisão informada (TI)"
                ));
    }
}
