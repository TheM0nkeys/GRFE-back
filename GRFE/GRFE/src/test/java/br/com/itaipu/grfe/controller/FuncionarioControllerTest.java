package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.FuncionarioRequest;
import br.com.itaipu.grfe.dto.response.FuncionarioResponse;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.exception.GlobalExceptionHandler;
import br.com.itaipu.grfe.service.FuncionarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {FuncionarioController.class, GlobalExceptionHandler.class})
class FuncionarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FuncionarioService funcionarioService;

    @Test
    void criar_comDadosValidos_retorna201() throws Exception {
        when(funcionarioService.criar(any(FuncionarioRequest.class)))
                .thenReturn(new FuncionarioResponse(
                        1L, "Felipe", "12345", "felipe@email.com", Set.of("Java")
                ));

        mockMvc.perform(post("/funcionarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Felipe",
                                  "matricula": "12345",
                                  "email": "felipe@email.com",
                                  "especialidadeIds": [1]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Felipe"));
    }

    @Test
    void buscarPorId_quandoFuncionarioNaoExiste_retorna404() throws Exception {
        when(funcionarioService.buscarPorId(99L))
                .thenThrow(new EntidadeNaoEncontradaException("Funcionário não encontrado: 99"));

        mockMvc.perform(get("/funcionarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Funcionário não encontrado: 99"));
    }
}
