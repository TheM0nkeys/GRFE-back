package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.LoginRequest;
import br.com.itaipu.grfe.dto.response.LoginResponse;
import br.com.itaipu.grfe.security.JwtService;
import br.com.itaipu.grfe.security.UsuarioDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.login(), request.senha())
            );

            UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();
            String token = jwtService.gerarToken(usuarioDetails);

            log.info("Login realizado com sucesso: login={}", request.login());

            String role = authentication.getAuthorities().iterator().next().getAuthority();
            return new LoginResponse(token, usuarioDetails.getUsername(), role);

        } catch (BadCredentialsException ex) {
            log.warn("Tentativa de login com credenciais inválidas: login={}", request.login());
            throw new BadCredentialsException("Usuário ou senha inválidos");
        }
    }
}