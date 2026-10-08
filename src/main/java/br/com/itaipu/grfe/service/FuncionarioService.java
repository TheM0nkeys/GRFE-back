package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.FuncionarioRequest;
import br.com.itaipu.grfe.dto.response.FuncionarioResponse;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class FuncionarioService {

    private static final Logger log = LoggerFactory.getLogger(FuncionarioService.class);

    private final FuncionarioRepository funcionarioRepository;
    private final EspecialidadesRepository especialidadesRepository;

    public FuncionarioService(FuncionarioRepository funcionarioRepository,
                              EspecialidadesRepository especialidadesRepository) {
        this.funcionarioRepository = funcionarioRepository;
        this.especialidadesRepository = especialidadesRepository;
    }

    public List<FuncionarioResponse> listarTodos() {
        return funcionarioRepository.findAll()
                .stream()
                .map(FuncionarioResponse::fromEntity)
                .toList();
    }

    public FuncionarioResponse buscarPorId(Long id) {
        return FuncionarioResponse.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public FuncionarioResponse criar(FuncionarioRequest request) {
        Funcionario funcionario = request.toEntity();
        funcionario.setEspecialidades(buscarEspecialidades(request.especialidadeIds()));
        funcionario = funcionarioRepository.save(funcionario);
        log.info("Funcionário criado: id={}, matricula={}", funcionario.getId(), funcionario.getMatricula());
        return FuncionarioResponse.fromEntity(funcionario);
    }

    @Transactional
    public FuncionarioResponse atualizar(Long id, FuncionarioRequest request) {
        Funcionario funcionario = buscarEntidadePorId(id);
        funcionario.setNome(request.nome());
        funcionario.setMatricula(request.matricula());
        funcionario.setEmail(request.email());
        funcionario.setEspecialidades(buscarEspecialidades(request.especialidadeIds()));
        funcionario = funcionarioRepository.save(funcionario);
        log.info("Funcionário atualizado: id={}", id);
        return FuncionarioResponse.fromEntity(funcionario);
    }

    @Transactional
    public void deletar(Long id) {
        if (!funcionarioRepository.existsById(id)) {
            log.warn("Tentativa de excluir funcionário inexistente: id={}", id);
            throw new EntidadeNaoEncontradaException("Funcionário não encontrado: " + id);
        }
        funcionarioRepository.deleteById(id);
        log.info("Funcionário removido: id={}", id);
    }

    private Funcionario buscarEntidadePorId(Long id) {
        return funcionarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Funcionário não encontrado: id={}", id);
                    return new EntidadeNaoEncontradaException("Funcionário não encontrado: " + id);
                });
    }

    private Set<Especialidades> buscarEspecialidades(Set<Long> ids) {
        Set<Especialidades> especialidades = new HashSet<>(especialidadesRepository.findAllById(ids));
        if (especialidades.size() != ids.size()) {
            log.warn("Uma ou mais especialidades informadas não existem: ids={}", ids);
            throw new EntidadeNaoEncontradaException("Uma ou mais especialidades informadas não existem");
        }
        return especialidades;
    }
}