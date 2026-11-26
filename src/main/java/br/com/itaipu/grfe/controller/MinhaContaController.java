package br.com.itaipu.grfe.controller;

import br.com.itaipu.grfe.dto.request.AlterarSenhaRequest;
import br.com.itaipu.grfe.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Minha conta", description = "Operações do próprio usuário autenticado")
@RestController
@RequestMapping("/minha-conta")
public class MinhaContaController {

    private final UsuarioService usuarioService;

    public MinhaContaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PatchMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@RequestBody @Valid AlterarSenhaRequest request) {
        usuarioService.alterarPropriaSenha(request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}