package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.DepartamentoRequest;
import br.com.itaipu.grfe.dto.response.DepartamentoResponse;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.exception.GlobalExceptionHandler;
import br.com.itaipu.grfe.service.DepartamentoService;
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
@ContextConfiguration(classes = {DepartamentoController.class, GlobalExceptionHandler.class})
class DepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartamentoService departamentoService;

    @Test
    void criar_comDadosValidos_retorna201() throws Exception {
        when(departamentoService.criar(any(DepartamentoRequest.class)))
                .thenReturn(new DepartamentoResponse(10L, "Infraestrutura", 1L, "TI"));

        mockMvc.perform(post("/departamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Infraestrutura",
                                  "divisaoId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Infraestrutura"));
    }

    @Test
    void buscarPorId_quandoDepartamentoNaoExiste_retorna404() throws Exception {
        when(departamentoService.buscarPorId(99L))
                .thenThrow(new EntidadeNaoEncontradaException("Departamento não encontrado: 99"));

        mockMvc.perform(get("/departamentos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Departamento não encontrado: 99"));
    }
}
