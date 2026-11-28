package br.com.itaipu.grfe.service;

import br.com.itaipu.grfe.dto.request.UsuarioRequest;
import br.com.itaipu.grfe.dto.response.UsuarioResponse;
import br.com.itaipu.grfe.entity.Funcionario;
import br.com.itaipu.grfe.entity.Usuario;
import br.com.itaipu.grfe.entity.enums.Role;
import br.com.itaipu.grfe.exception.EntidadeNaoEncontradaException;
import br.com.itaipu.grfe.repository.FuncionarioRepository;
import br.com.itaipu.grfe.repository.UsuarioRepository;
import br.com.itaipu.grfe.security.UsuarioLogado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private static final int TAMANHO_MINIMO_SENHA = 12;
    private static final int LIMITE_BYTES_SENHA = 72;

    private final UsuarioRepository usuarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioLogado usuarioLogado;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          FuncionarioRepository funcionarioRepository,
                          PasswordEncoder passwordEncoder,
                          UsuarioLogado usuarioLogado) {
        this.usuarioRepository = usuarioRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioLogado = usuarioLogado;
    }

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::fromEntity)
                .toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        String login = request.login().trim();
        validarSenha(request.senha(), login);

        if (usuarioRepository.existsByLoginIgnoreCase(login)) {
            log.warn("Tentativa de criar usuário com login já existente: login={}", login);
            throw new IllegalArgumentException("Já existe um usuário com este login.");
        }

        Funcionario funcionario = funcionarioRepository.findById(request.funcionarioId())
                .orElseThrow(() -> {
                    log.warn("Funcionário não encontrado ao criar usuário: funcionarioId={}", request.funcionarioId());
                    return new EntidadeNaoEncontradaException("Funcionário não encontrado: " + request.funcionarioId());
                });

        if (usuarioRepository.existsByFuncionarioId(funcionario.getId())) {
            log.warn("Funcionário já possui usuário: funcionarioId={}", funcionario.getId());
            throw new IllegalArgumentException("Este funcionário já possui um usuário.");
        }

        Usuario usuario = new Usuario();
        usuario.setLogin(login);
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRole(request.role());
        usuario.setFuncionario(funcionario);

        usuario = usuarioRepository.save(usuario);
        log.info("Usuário criado: id={}, login={}, role={}", usuario.getId(), login, request.role());
        return UsuarioResponse.fromEntity(usuario);
    }

    @Transactional
    public void redefinirSenha(Long id, String novaSenha) {
        Usuario usuario = buscarEntidade(id);
        validarSenha(novaSenha, usuario.getLogin());

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        log.info("Senha redefinida por administrador: usuarioId={}", id);
    }

    @Transactional
    public void alterarPropriaSenha(String senhaAtual, String novaSenha) {
        Long id = usuarioLogado.obter().getId();
        Usuario usuario = buscarEntidade(id);

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            log.warn("Troca de senha com senha atual incorreta: usuarioId={}", id);
            throw new IllegalArgumentException("Senha atual incorreta.");
        }
        if (passwordEncoder.matches(novaSenha, usuario.getSenhaHash())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual.");
        }
        validarSenha(novaSenha, usuario.getLogin());

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        log.info("Usuário alterou a própria senha: usuarioId={}", id);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario alvo = buscarEntidade(id);

        if (usuarioLogado.obter().getId().equals(id)) {
            throw new IllegalArgumentException("Você não pode excluir o próprio usuário.");
        }
        if (alvo.getRole() == Role.ADMIN && usuarioRepository.countByRole(Role.ADMIN) <= 1) {
            throw new IllegalArgumentException("Não é possível excluir o último administrador.");
        }

        usuarioRepository.delete(alvo);
        log.info("Usuário removido: id={}, login={}", id, alvo.getLogin());
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Usuário não encontrado: id={}", id);
                    return new EntidadeNaoEncontradaException("Usuário não encontrado: " + id);
                });
    }

    private void validarSenha(String senha, String login) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        if (senha.getBytes(StandardCharsets.UTF_8).length > LIMITE_BYTES_SENHA) {
            throw new IllegalArgumentException(
                    "A senha não pode passar de " + LIMITE_BYTES_SENHA + " bytes. Use uma senha mais curta.");
        }
        if (senha.equalsIgnoreCase(login)) {
            throw new IllegalArgumentException("A senha não pode ser igual ao login.");
        }
    }
}
