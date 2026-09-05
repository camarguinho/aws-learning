package br.com.camarguinho.awslearning.common.exception;

/**
 * Lançada quando um recurso solicitado (produto, pedido, etc.) não existe.
 * Mapeada pelo {@link GlobalExceptionHandler} para HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
