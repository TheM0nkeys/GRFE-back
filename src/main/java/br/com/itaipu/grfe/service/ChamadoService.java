package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.ChamadoRequest;
import br.com.itaipu.grfe.dto.response.ChamadoResponse;
import br.com.itaipu.grfe.entity.Chamado;
import br.com.itaipu.grfe.entity.Especialidades;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.ChamadoRepository;
import br.com.itaipu.grfe.repository.EscalaRepository;
import br.com.itaipu.grfe.repository.EspecialidadesRepository;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import br.com.itaipu.grfe.security.UsuarioLogado;
import br.com.itaipu.grfe.specification.ChamadoSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChamadoService {

    private static final Logger log = LoggerFactory.getLogger(ChamadoService.class);

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private static final String MSG_FECHADO =
            "O acionamento está FECHADO e não pode ser alterado. Solicite a reabertura a um administrador.";

    private final ChamadoRepository chamadoRepository;
    private final EspecialidadesRepository especialidadesRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final EscalaRepository escalaRepository;
    private final UsuarioLogado usuarioLogado;

    public ChamadoService(ChamadoRepository chamadoRepository,
                          EspecialidadesRepository especialidadesRepository,
                          FuncionarioRepository funcionarioRepository,
                          EscalaRepository escalaRepository,
                          UsuarioLogado usuarioLogado) {
        this.chamadoRepository = chamadoRepository;
        this.especialidadesRepository = especialidadesRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.escalaRepository = escalaRepository;
        this.usuarioLogado = usuarioLogado;
    }

    public List<ChamadoResponse> listar(StatusChamado status,
                                        Long especialidadeId,
                                        Long plantonistaId,
                                        Long divisaoId,
                                        Long departamentoId,
                                        LocalDateTime dataInicio,
                                        LocalDateTime dataFim) {
        log.info("Listando acionamentos com filtros: status={}, especialidadeId={}, plantonistaId={}, " +
                        "divisaoId={}, departamentoId={}, dataInicio={}, dataFim={}",
                status, especialidadeId, plantonistaId, divisaoId, departamentoId, dataInicio, dataFim);

        var filtro = ChamadoSpecification.comFiltros(
                status, especialidadeId, plantonistaId, divisaoId, departamentoId, dataInicio, dataFim);

        return chamadoRepository.findAll(filtro)
                .stream()
                .map(ChamadoResponse::fromEntity)
                .toList();
    }

    public Page<ChamadoResponse> listarPaginado(StatusChamado status,
                                                Long especialidadeId,
                                                Long plantonistaId,
                                                Long divisaoId,
                                                Long departamentoId,
                                                LocalDateTime dataInicio,
                                                LocalDateTime dataFim,
                                                int page,
                                                int size) {
        if (page < 0 || size < 1 || size > TAMANHO_MAXIMO_PAGINA) {
            throw new IllegalArgumentException(
                    "Paginação inválida: page deve ser maior ou igual a 0 e size deve estar entre 1 e "
                            + TAMANHO_MAXIMO_PAGINA + ".");
        }

        log.info("Listando acionamentos paginados: page={}, size={}, status={}, especialidadeId={}, " +
                        "plantonistaId={}, divisaoId={}, departamentoId={}, dataInicio={}, dataFim={}",
                page, size, status, especialidadeId, plantonistaId, divisaoId, departamentoId, dataInicio, dataFim);

        var filtro = ChamadoSpecification.comFiltros(
                status, especialidadeId, plantonistaId, divisaoId, departamentoId, dataInicio, dataFim);

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "dataHoraAcionamento")
                        .and(Sort.by(Sort.Direction.DESC, "id")));

        return chamadoRepository.findAll(filtro, pageable).map(ChamadoResponse::fromEntity);
    }

    public ChamadoResponse buscarPorId(Long id) {
        return ChamadoResponse.fromEntity(buscarEntidadePorId(id));
    }

    @Transactional
    public ChamadoResponse atualizarStatus(Long id, StatusChamado status) {
        Chamado chamado = buscarEntidadeParaAtualizar(id);
        exigirEditavel(chamado);
        validarTransicao(chamado, status);
        StatusChamado statusAnterior = chamado.getStatus();
        chamado.setStatus(status);
        chamado = chamadoRepository.save(chamado);
        log.info("Status do acionamento alterado: id={}, statusAnterior={}, statusNovo={}",
                id, statusAnterior, status);
        return ChamadoResponse.fromEntity(chamado);
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest request) {
        Chamado chamado = request.toEntity();
        chamado.setStatus(StatusChamado.ABERTO);
        vincularRelacoes(chamado, request);
        chamado.setUsuarioResponsavel(buscarResponsavelLogado());
        chamado = chamadoRepository.save(chamado);
        log.info("Acionamento criado: id={}, especialidadeId={}, plantonistaId={}, responsavelId={}, numeroIncidente={}",
                chamado.getId(), request.especialidadeId(), request.plantonistaId(),
                chamado.getUsuarioResponsavel().getId(), request.numeroIncidente());
        return ChamadoResponse.fromEntity(chamado);
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest request) {
        Chamado chamado = buscarEntidadeParaAtualizar(id);
        exigirEditavel(chamado);
        validarTransicao(chamado, request.status());
        chamado.setDataHoraAcionamento(request.dataHoraAcionamento());
        chamado.setMotivo(request.motivo());
        chamado.setNumeroIncidente(request.numeroIncidente());
        chamado.setStatus(request.status());
        vincularRelacoes(chamado, request);
        chamado = chamadoRepository.save(chamado);
        log.info("Acionamento atualizado: id={}, status={}", id, request.status());
        return ChamadoResponse.fromEntity(chamado);
    }

    @Transactional
    public ChamadoResponse reabrir(Long id) {
        Chamado chamado = buscarEntidadeParaAtualizar(id);
        if (chamado.getStatus() != StatusChamado.FECHADO) {
            log.warn("Tentativa de reabrir acionamento que não está FECHADO: id={}, status={}",
                    id, chamado.getStatus());
            throw new IllegalArgumentException("Somente acionamentos FECHADOS podem ser reabertos.");
        }
        chamado.setStatus(StatusChamado.ABERTO);
        chamado = chamadoRepository.save(chamado);
        log.info("Acionamento reaberto: id={}", id);
        return ChamadoResponse.fromEntity(chamado);
    }

    @Transactional
    public void deletar(Long id) {
        Chamado chamado = buscarEntidadePorId(id);
        if (!chamado.getHistorico().isEmpty()) {
            throw new IllegalArgumentException(
                    "Não é possível excluir um acionamento que possui histórico.");
        }
        chamadoRepository.deleteById(id);
        log.info("Acionamento removido: id={}", id);
    }

    private Chamado buscarEntidadePorId(Long id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Acionamento não encontrado: id={}", id);
                    return new EntidadeNaoEncontradaException("Acionamento não encontrado: " + id);
                });
    }

    private Chamado buscarEntidadeParaAtualizar(Long id) {
        return chamadoRepository.buscarParaAtualizar(id)
                .orElseThrow(() -> {
                    log.warn("Acionamento não encontrado: id={}", id);
                    return new EntidadeNaoEncontradaException("Acionamento não encontrado: " + id);
                });
    }

    private void exigirEditavel(Chamado chamado) {
        if (chamado.getStatus() == StatusChamado.FECHADO) {
            log.warn("Tentativa de alterar acionamento FECHADO: id={}", chamado.getId());
            throw new IllegalArgumentException(MSG_FECHADO);
        }
    }

    private void validarTransicao(Chamado chamado, StatusChamado destino) {
        StatusChamado atual = chamado.getStatus();
        if (atual == null || atual.podeIrPara(destino)) {
            return;
        }
        log.warn("Transição de status não permitida: id={}, de={}, para={}", chamado.getId(), atual, destino);
        throw new IllegalArgumentException(
                "Transição de status não permitida: de " + atual + " para " + destino + ".");
    }

    private Funcionario buscarResponsavelLogado() {
        Long funcionarioId = usuarioLogado.funcionarioId();
        return funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> {
                    log.warn("Usuário responsável não encontrado ao vincular acionamento: usuarioResponsavelId={}", funcionarioId);
                    return new EntidadeNaoEncontradaException("Usuário responsável não encontrado: " + funcionarioId);
                });
    }

    private void vincularRelacoes(Chamado chamado, ChamadoRequest request) {
        Especialidades especialidade = especialidadesRepository.findById(request.especialidadeId())
                .orElseThrow(() -> {
                    log.warn("Especialidade não encontrada ao vincular acionamento: especialidadeId={}", request.especialidadeId());
                    return new EntidadeNaoEncontradaException("Especialidade não encontrada: " + request.especialidadeId());
                });

        Funcionario plantonista = funcionarioRepository.findById(request.plantonistaId())
                .orElseThrow(() -> {
                    log.warn("Plantonista não encontrado ao vincular acionamento: plantonistaId={}", request.plantonistaId());
                    return new EntidadeNaoEncontradaException("Plantonista não encontrado: " + request.plantonistaId());
                });

        if (!plantonista.getEspecialidades().contains(especialidade)) {
            log.warn(
                    "Plantonista sem especialidade: plantonistaId={}, especialidadeId={}",
                    plantonista.getId(),
                    especialidade.getId()
            );

            throw new IllegalArgumentException(
                    "O plantonista informado não possui a especialidade do chamado."
            );
        }

        if (!escalaRepository.plantonistaEmEscala(
                especialidade.getId(), plantonista.getId(), request.dataHoraAcionamento())) {
            log.warn("Plantonista fora de escala: plantonistaId={}, especialidadeId={}, dataHoraAcionamento={}",
                    plantonista.getId(), especialidade.getId(), request.dataHoraAcionamento());

            throw new IllegalArgumentException(
                    "O plantonista informado não está em escala para esta especialidade na data/hora do acionamento.");
        }

        chamado.setEspecialidade(especialidade);
        chamado.setPlantonista(plantonista);
    }
}
