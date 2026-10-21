package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.DivisaoRequest;
import br.com.itaipu.grfe.dto.response.DivisaoResponse;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.exception.GlobalExceptionHandler;
import br.com.itaipu.grfe.service.DivisaoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {DivisaoController.class, GlobalExceptionHandler.class})
class DivisaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DivisaoService divisaoService;

    @Test
    void criar_comDadosValidos_retorna201() throws Exception {
        when(divisaoService.criar(any(DivisaoRequest.class)))
                .thenReturn(new DivisaoResponse(1L, "TI"));

        mockMvc.perform(post("/divisoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"TI\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("TI"));
    }

    @Test
    void buscarPorId_quandoDivisaoNaoExiste_retorna404() throws Exception {
        when(divisaoService.buscarPorId(99L))
                .thenThrow(new EntidadeNaoEncontradaException("Divisão não encontrada: 99"));

        mockMvc.perform(get("/divisoes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Divisão não encontrada: 99"));
    }
}
