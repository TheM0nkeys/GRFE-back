package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.EspecialidadesRequest;
import br.com.itaipu.grfe.dto.response.EspecialidadeResponse;
import br.com.itaipu.grfe.service.EspecialidadesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Especialidades", description = "Especialidades de sobreaviso")
@RestController
@RequestMapping("/especialidades")
public class EspecialidadesController {

    private final EspecialidadesService especialidadesService;

    public EspecialidadesController(EspecialidadesService especialidadesService) {
        this.especialidadesService = especialidadesService;
    }

    @GetMapping
    public ResponseEntity<List<EspecialidadeResponse>> listarTodas() {
        return ResponseEntity.ok(especialidadesService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadesService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EspecialidadeResponse> criar(@RequestBody @Valid EspecialidadesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(especialidadesService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadeResponse> atualizar(@PathVariable Long id,
                                                           @RequestBody @Valid EspecialidadesRequest request) {
        return ResponseEntity.ok(especialidadesService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        especialidadesService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}