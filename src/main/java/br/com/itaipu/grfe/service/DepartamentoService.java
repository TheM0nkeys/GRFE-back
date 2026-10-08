package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.DepartamentoRequest;
import br.com.itaipu.grfe.dto.response.DepartamentoResponse;
import br.com.itaipu.grfe.entity.Departamento;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.DepartamentoRepository;
import br.com.itaipu.grfe.repository.DivisaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DepartamentoService {

    private static final Logger log = LoggerFactory.getLogger(DepartamentoService.class);

    private final DepartamentoRepository departamentoRepository;
    private final DivisaoRepository divisaoRepository;

    public DepartamentoService(DepartamentoRepository departamentoRepository,
                               DivisaoRepository divisaoRepository) {
        this.departamentoRepository = departamentoRepository;
        this.divisaoRepository = divisaoRepository;
    }

    public List<DepartamentoResponse> listarTodos() {
        return departamentoRepository.findAll()
                .stream()
                .map(DepartamentoResponse::fromEntity)
                .toList();
    }

    public DepartamentoResponse buscarPorId(Long id) {
        return DepartamentoResponse.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public DepartamentoResponse criar(DepartamentoRequest request) {
        Departamento departamento = request.toEntity();
        departamento.setDivisao(buscarDivisao(request.divisaoId()));
        departamento = departamentoRepository.save(departamento);
        log.info("Departamento criado: id={}, nome={}, divisaoId={}",
                departamento.getId(), departamento.getNome(), request.divisaoId());
        return DepartamentoResponse.fromEntity(departamento);
    }

    @Transactional
    public DepartamentoResponse atualizar(Long id, DepartamentoRequest request) {
        Departamento departamento = buscarEntidadePorId(id);
        departamento.setNome(request.nome());
        departamento.setDivisao(buscarDivisao(request.divisaoId()));
        departamento = departamentoRepository.save(departamento);
        log.info("Departamento atualizado: id={}", id);
        return DepartamentoResponse.fromEntity(departamento);
    }

    @Transactional
    public void deletar(Long id) {
        if (!departamentoRepository.existsById(id)) {
            log.warn("Tentativa de excluir departamento inexistente: id={}", id);
            throw new EntidadeNaoEncontradaException("Departamento não encontrado: " + id);
        }
        departamentoRepository.deleteById(id);
        log.info("Departamento removido: id={}", id);
    }

    private Departamento buscarEntidadePorId(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Departamento não encontrado: id={}", id);
                    return new EntidadeNaoEncontradaException("Departamento não encontrado: " + id);
                });
    }

    private Divisao buscarDivisao(Long divisaoId) {
        return divisaoRepository.findById(divisaoId)
                .orElseThrow(() -> {
                    log.warn("Divisão não encontrada ao vincular departamento: divisaoId={}", divisaoId);
                    return new EntidadeNaoEncontradaException("Divisão não encontrada: " + divisaoId);
                });
    }
}