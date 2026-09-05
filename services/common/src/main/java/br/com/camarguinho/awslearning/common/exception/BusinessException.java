package br.com.camarguinho.awslearning.common.exception;

/**
 * Lançada quando uma regra de negócio é violada (ex.: estoque insuficiente).
 * Mapeada pelo {@link GlobalExceptionHandler} para HTTP 422.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
