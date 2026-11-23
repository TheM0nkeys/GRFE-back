package br.com.itaipu.grfe.entity.enums;

public enum StatusChamado {
    ABERTO,
    EM_ANDAMENTO,
    FECHADO;

    public boolean podeIrPara(StatusChamado destino) {
        if (this == destino) {
            return true;
        }
        return switch (this) {
            case ABERTO -> destino == EM_ANDAMENTO || destino == FECHADO;
            case EM_ANDAMENTO -> destino == FECHADO;
            case FECHADO -> false;
        };
    }
}