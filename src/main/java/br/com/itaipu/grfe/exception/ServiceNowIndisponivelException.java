package br.com.itaipu.grfe.exception;

public class ServiceNowIndisponivelException extends RuntimeException {

    public ServiceNowIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public ServiceNowIndisponivelException(
            String mensagem,
            Throwable causa
    ) {
        super(mensagem, causa);
    }
}