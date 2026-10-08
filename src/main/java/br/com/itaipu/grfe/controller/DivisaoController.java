package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.DivisaoRequest;
import br.com.itaipu.grfe.dto.response.DivisaoResponse;
import br.com.itaipu.grfe.service.DivisaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Divisões", description = "Cadastro de divisões da estrutura organizacional")
@RestController
@RequestMapping("/divisoes")
public class DivisaoController {

    private final DivisaoService divisaoService;

    public DivisaoController(DivisaoService divisaoService) {
        this.divisaoService = divisaoService;
    }

    @GetMapping
    public ResponseEntity<List<DivisaoResponse>> listarTodas() {
        return ResponseEntity.ok(divisaoService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DivisaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(divisaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<DivisaoResponse> criar(@RequestBody @Valid DivisaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(divisaoService.criar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DivisaoResponse> atualizar(@PathVariable Long id,
                                                     @RequestBody @Valid DivisaoRequest request) {
        return ResponseEntity.ok(divisaoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        divisaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}