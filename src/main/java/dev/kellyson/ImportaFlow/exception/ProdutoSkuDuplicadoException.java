package dev.kellyson.ImportaFlow.exception;

public class ProdutoSkuDuplicadoException extends RuntimeException {

    public ProdutoSkuDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
