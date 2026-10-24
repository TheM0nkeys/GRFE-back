package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.EspecialidadesRequest;
import br.com.itaipu.grfe.dto.response.EspecialidadeResponse;
import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.repository.DepartamentoRepository;
import br.com.itaipu.grfe.repository.DivisaoRepository;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EspecialidadesServiceTest {

    @Mock
    private EspecialidadesRepository especialidadesRepository;

    @Mock
    private DivisaoRepository divisaoRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    private EspecialidadesService service;

    @BeforeEach
    void setUp() {
        service = new EspecialidadesService(
                especialidadesRepository,
                divisaoRepository,
                departamentoRepository
        );
    }

    @Test
    void criar_quandoDepartamentoPertenceADivisao_salvaEspecialidade() {
        Divisao divisao = divisao(1L, "TI");
        Departamento departamento = departamento(10L, "Infraestrutura", divisao);
        EspecialidadesRequest request = new EspecialidadesRequest(
                "Redes", "Suporte de redes", 1L, 10L
        );

        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisao));
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento));
        when(especialidadesRepository.save(any(Especialidades.class))).thenAnswer(invocation -> {
            Especialidades especialidade = invocation.getArgument(0);
            especialidade.setId(100L);
            return especialidade;
        });

        EspecialidadeResponse response = service.criar(request);

        assertEquals(100L, response.id());
        assertEquals("Redes", response.nome());
        assertEquals(1L, response.divisaoId());
        assertEquals(10L, response.departamentoId());
        verify(especialidadesRepository).save(any(Especialidades.class));
    }

    @Test
    void criar_quandoDepartamentoNaoPertenceADivisao_lancaIllegalArgumentException() {
        Divisao divisaoInformada = divisao(1L, "TI");
        Divisao outraDivisao = divisao(2L, "Administrativa");
        Departamento departamento = departamento(10L, "Financeiro", outraDivisao);
        EspecialidadesRequest request = new EspecialidadesRequest(
                "Redes", "Suporte de redes", 1L, 10L
        );

        when(divisaoRepository.findById(1L)).thenReturn(Optional.of(divisaoInformada));
        when(departamentoRepository.findById(10L)).thenReturn(Optional.of(departamento));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.criar(request)
        );

        assertEquals(
                "O departamento informado (Financeiro) não pertence à divisão informada (TI)",
                exception.getMessage()
        );
        verify(especialidadesRepository, never()).save(any());
    }

    private Divisao divisao(Long id, String nome) {
        Divisao divisao = new Divisao();
        divisao.setId(id);
        divisao.setNome(nome);
        return divisao;
    }

    private Departamento departamento(Long id, String nome, Divisao divisao) {
        Departamento departamento = new Departamento();
        departamento.setId(id);
        departamento.setNome(nome);
        departamento.setDivisao(divisao);
        return departamento;
    }
}
