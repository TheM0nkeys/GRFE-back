package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.DivisaoRequest;
import br.com.itaipu.grfe.dto.response.DivisaoResponse;
import br.com.itaipu.grfe.entity.Divisao;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.DivisaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DivisaoService {

    private static final Logger log = LoggerFactory.getLogger(DivisaoService.class);

    private final DivisaoRepository divisaoRepository;

    public DivisaoService(DivisaoRepository divisaoRepository) {
        this.divisaoRepository = divisaoRepository;
    }

    public List<DivisaoResponse> listarTodas() {
        return divisaoRepository.findAll()
                .stream()
                .map(DivisaoResponse::fromEntity)
                .toList();
    }

    public DivisaoResponse buscarPorId(Long id) {
        Divisao divisao = buscarEntidadePorId(id);
        return DivisaoResponse.fromEntity(divisao);
    }

    @Transactional
    public DivisaoResponse criar(DivisaoRequest request) {
        Divisao divisao = request.toEntity();
        divisao = divisaoRepository.save(divisao);
        log.info("Divisão criada: id={}, nome={}", divisao.getId(), divisao.getNome());
        return DivisaoResponse.fromEntity(divisao);
    }

    @Transactional
    public DivisaoResponse atualizar(Long id, DivisaoRequest request) {
        Divisao divisao = buscarEntidadePorId(id);
        divisao.setNome(request.nome());
        divisao = divisaoRepository.save(divisao);
        log.info("Divisão atualizada: id={}", id);
        return DivisaoResponse.fromEntity(divisao);
    }

    @Transactional
    public void deletar(Long id) {
        if (!divisaoRepository.existsById(id)) {
            log.warn("Tentativa de excluir divisão inexistente: id={}", id);
            throw new EntidadeNaoEncontradaException("Divisão não encontrada: " + id);
        }
        divisaoRepository.deleteById(id);
        log.info("Divisão removida: id={}", id);
    }

    private Divisao buscarEntidadePorId(Long id) {
        return divisaoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Divisão não encontrada: id={}", id);
                    return new EntidadeNaoEncontradaException("Divisão não encontrada: " + id);
                });
    }
}
