package br.com.itaipu.grfe.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UsuarioLogado {

    public UsuarioDetails obter() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !(autenticacao.getPrincipal() instanceof UsuarioDetails detalhes)) {
            throw new AuthenticationCredentialsNotFoundException("Nenhum usuário autenticado.");
        }
        return detalhes;
    }

    public Long funcionarioId() {
        Long funcionarioId = obter().getFuncionarioId();

        if (funcionarioId == null) {
            throw new IllegalArgumentException(
                    "Seu usuário não está vinculado a um funcionário. Peça a um administrador para vincular.");
        }
        return funcionarioId;
    }
}