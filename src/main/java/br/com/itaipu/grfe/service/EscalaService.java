package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.EscalaRequest;
import br.com.itaipu.grfe.dto.response.EscalaResponse;
import br.com.itaipu.grfe.entity.Escala;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.EscalaRepository;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EscalaService {

    private static final Logger log = LoggerFactory.getLogger(EscalaService.class);

    private final EscalaRepository escalaRepository;
    private final EspecialidadesRepository especialidadesRepository;
    private final FuncionarioRepository funcionarioRepository;

    public EscalaService(EscalaRepository escalaRepository,
                         EspecialidadesRepository especialidadesRepository,
                         FuncionarioRepository funcionarioRepository) {
        this.escalaRepository = escalaRepository;
        this.especialidadesRepository = especialidadesRepository;
        this.funcionarioRepository = funcionarioRepository;
    }

    public List<EscalaResponse> listarTodas() {
        return escalaRepository.findAll()
                .stream()
                .map(EscalaResponse::fromEntity)
                .toList();
    }

    public EscalaResponse buscarPorId(Long id) {
        return EscalaResponse.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public EscalaResponse criar(EscalaRequest request) {

        if (request.dataHoraFim().isBefore(request.dataHoraInicio())
                || request.dataHoraFim().isEqual(request.dataHoraInicio())) {
            throw new IllegalArgumentException(
                    "A data/hora de fim deve ser posterior à data/hora de início."
            );
        }

        Escala escala = request.toEntity();
        vincularEspecialidadeEFuncionario(escala, request);
        escala = escalaRepository.save(escala);
        log.info("Escala criada: id={}, especialidadeId={}, funcionarioId={}",
                escala.getId(), request.especialidadeId(), request.funcionarioId());
        return EscalaResponse.fromEntity(escala);
    }

    @Transactional
    public EscalaResponse atualizar(Long id, EscalaRequest request) {

        if (request.dataHoraFim().isBefore(request.dataHoraInicio())
                || request.dataHoraFim().isEqual(request.dataHoraInicio())) {
            throw new IllegalArgumentException(
                    "A data/hora de fim deve ser posterior à data/hora de início."
            );
        }

        Escala escala = buscarEntidadePorId(id);
        escala.setDataHoraInicio(request.dataHoraInicio());
        escala.setDataHoraFim(request.dataHoraFim());
        vincularEspecialidadeEFuncionario(escala, request);
        escala = escalaRepository.save(escala);
        log.info("Escala atualizada: id={}", id);
        return EscalaResponse.fromEntity(escala);
    }

    @Transactional
    public void deletar(Long id) {
        if (!escalaRepository.existsById(id)) {
            log.warn("Tentativa de excluir escala inexistente: id={}", id);
            throw new EntidadeNaoEncontradaException("Escala não encontrada: " + id);
        }
        escalaRepository.deleteById(id);
        log.info("Escala removida: id={}", id);
    }

    private Escala buscarEntidadePorId(Long id) {
        return escalaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Escala não encontrada: id={}", id);
                    return new EntidadeNaoEncontradaException("Escala não encontrada: " + id);
                });
    }

    private void vincularEspecialidadeEFuncionario(Escala escala, EscalaRequest request) {
        Especialidades especialidade = especialidadesRepository.findById(request.especialidadeId())
                .orElseThrow(() -> {
                    log.warn("Especialidade não encontrada ao vincular escala: especialidadeId={}", request.especialidadeId());
                    return new EntidadeNaoEncontradaException("Especialidade não encontrada: " + request.especialidadeId());
                });

        Funcionario funcionario = funcionarioRepository.findById(request.funcionarioId())
                .orElseThrow(() -> {
                    log.warn("Funcionário não encontrado ao vincular escala: funcionarioId={}", request.funcionarioId());
                    return new EntidadeNaoEncontradaException("Funcionário não encontrado: " + request.funcionarioId());
                });

        if (!funcionario.getEspecialidades().contains(especialidade)) {
            log.warn(
                    "Funcionário sem especialidade para escala: funcionarioId={}, especialidadeId={}",
                    funcionario.getId(),
                    especialidade.getId()
            );

            throw new IllegalArgumentException(
                    "O funcionário informado não possui a especialidade selecionada."
            );
        }

        escala.setEspecialidade(especialidade);
        escala.setFuncionario(funcionario);
    }
}