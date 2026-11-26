package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.ChamadoRequest;
import br.com.itaipu.grfe.dto.request.HistoricoAcionamentoRequest;
import br.com.itaipu.grfe.dto.response.ChamadoResponse;
import br.com.itaipu.grfe.dto.response.HistoricoAcionamentoResponse;
import br.com.itaipu.grfe.entity.enums.StatusChamado;
import br.com.itaipu.grfe.service.ChamadoService;
import br.com.itaipu.grfe.service.HistoricoAcionamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Chamados (Acionamentos)", description = "Registro e acompanhamento de acionamentos de sobreaviso")
@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    private static final int TAMANHO_PADRAO_PAGINA = 20;

    private final ChamadoService chamadoService;
    private final HistoricoAcionamentoService historicoAcionamentoService;

    public ChamadoController(ChamadoService chamadoService,
                             HistoricoAcionamentoService historicoAcionamentoService) {
        this.chamadoService = chamadoService;
        this.historicoAcionamentoService = historicoAcionamentoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChamadoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return  ResponseEntity.ok(chamadoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ChamadoResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusChamado status) {

        return ResponseEntity.ok(
                chamadoService.atualizarStatus(id, status)
        );
    }

    // Só ADMIN (regra no SecurityConfig)
    @PostMapping("/{id}/reabrir")
    public ResponseEntity<ChamadoResponse> reabrir(@PathVariable Long id) {
        return ResponseEntity.ok(chamadoService.reabrir(id));
    }

    @GetMapping
    public ResponseEntity<List<ChamadoResponse>> listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Long especialidadeId,
            @RequestParam(required = false) Long plantonistaId,
            @RequestParam(required = false) Long divisaoId,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dataFim,
            @RequestParam(required = false) Integer page,                                    // NOVO
            @RequestParam(required = false) Integer size                                     // NOVO
    ) {
        if (page == null && size == null) {
            return ResponseEntity.ok(chamadoService.listar(
                    status, especialidadeId, plantonistaId, divisaoId, departamentoId, dataInicio, dataFim));
        }

        Page<ChamadoResponse> pagina = chamadoService.listarPaginado(
                status, especialidadeId, plantonistaId, divisaoId, departamentoId, dataInicio, dataFim,
                page == null ? 0 : page,
                size == null ? TAMANHO_PADRAO_PAGINA : size);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(pagina.getTotalElements()))
                .header("X-Total-Pages", String.valueOf(pagina.getTotalPages()))
                .header("X-Page", String.valueOf(pagina.getNumber()))
                .header("X-Page-Size", String.valueOf(pagina.getSize()))
                .body(pagina.getContent());
    }

    @PostMapping
    public ResponseEntity<ChamadoResponse> criar(@RequestBody @Valid ChamadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chamadoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChamadoResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid ChamadoRequest request) {
        return ResponseEntity.ok(chamadoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        chamadoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<List<HistoricoAcionamentoResponse>> listarHistorico(@PathVariable Long id) {
        return ResponseEntity.ok(historicoAcionamentoService.listarPorChamado(id));
    }

    @PostMapping("/{id}/historico")
    public ResponseEntity<HistoricoAcionamentoResponse> registrarHistorico(
            @PathVariable Long id,
            @RequestBody @Valid HistoricoAcionamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historicoAcionamentoService.registrar(id, request));
    }
}