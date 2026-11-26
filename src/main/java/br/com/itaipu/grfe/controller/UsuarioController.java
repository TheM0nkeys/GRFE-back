package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.RedefinirSenhaRequest;
import br.com.itaipu.grfe.dto.request.UsuarioRequest;
import br.com.itaipu.grfe.dto.response.UsuarioResponse;
import br.com.itaipu.grfe.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuários", description = "Cadastro e gestão de usuários (somente ADMIN)")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @PatchMapping("/{id}/senha")
    public ResponseEntity<Void> redefinirSenha(@PathVariable Long id,
                                               @RequestBody @Valid RedefinirSenhaRequest request) {
        usuarioService.redefinirSenha(id, request.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}