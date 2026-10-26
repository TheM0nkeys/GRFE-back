package br.com.itaipu.grfe.support;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("security-test")
@RestController
public class ProbeController {

    @GetMapping("/teste/protegido")
    public String protegido() {
        return "ok";
    }

    @GetMapping("/teste/credenciais")
    public String credenciais() {
        throw new BadCredentialsException("credenciais inválidas");
    }

    @GetMapping("/teste/negado")
    public String negado() {
        throw new AccessDeniedException("sem permissão");
    }

    @GetMapping("/swagger-ui.html")
    public String swaggerUi() {
        return "swagger";
    }

    @GetMapping("/v3/api-docs")
    public String apiDocs() {
        return "docs";
    }
}