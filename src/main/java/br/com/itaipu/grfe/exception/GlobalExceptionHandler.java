package br.com.itaipu.grfe.exception;

import br.com.itaipu.grfe.dto.response.ErroResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {

        ErroResponse erro = ErroResponse.of(
                HttpStatus.CONFLICT.value(),
                "A operação não pode ser realizada porque o registro possui dados relacionados."
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> handleEntidadeNaoEncontradaException(EntidadeNaoEncontradaException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErroResponse erro = ErroResponse.of(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(this::formatarErroDeCampo)
                .toList();

        ErroResponse erro = ErroResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos dados enviados",
                detalhes
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Tentativa de login com credenciais inválidas");

        ErroResponse erro = ErroResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Login ou senha inválidos"
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());

        ErroResponse erro = ErroResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Você não tem permissão para acessar este recurso"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> handleMensagemIlegivel(HttpMessageNotReadableException ex) {
        log.warn("Corpo da requisição inválido: {}", ex.getMessage());

        ErroResponse erro = ErroResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Corpo da requisição inválido ou malformado. Verifique o JSON e os valores dos campos "
                        + "(ex.: status deve ser ABERTO, EM_ANDAMENTO ou FECHADO)."
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> handleTipoParametroInvalido(MethodArgumentTypeMismatchException ex) {
        log.warn("Parâmetro inválido: nome={}, valor={}", ex.getName(), ex.getValue());

        ErroResponse erro = ErroResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue()
        );
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenericException(Exception ex) {

        log.error("Erro inesperado na aplicação", ex);

        ErroResponse erro = ErroResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno inesperado"
        );

        return ResponseEntity.internalServerError().body(erro);
    }

    private String formatarErroDeCampo(FieldError erro) {
        return erro.getField() + ": " + erro.getDefaultMessage();
    }
}