package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.EscalaRequest;
import br.com.itaipu.grfe.dto.response.EscalaResponse;
import br.com.itaipu.grfe.service.EscalaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Escalas", description = "Escalas semanais de plantão por especialidade")
@RestController
@RequestMapping("/escalas")
public class EscalaController {

    private final EscalaService escalaService;

    public EscalaController(EscalaService escalaService) {
        this.escalaService = escalaService;
    }

    @GetMapping
    public ResponseEntity<List<EscalaResponse>> listarTodas() {
        return ResponseEntity.ok(escalaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EscalaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(escalaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EscalaResponse> criar(@RequestBody @Valid EscalaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(escalaService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EscalaResponse> atualizar(@PathVariable Long id,
                                                    @RequestBody @Valid EscalaRequest request) {
        return ResponseEntity.ok(escalaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        escalaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}