package br.com.camarguinho.awslearning.common.exception;

/**
 * Lançada pelos métodos de fallback do Resilience4j quando uma dependência
 * downstream (outro microsserviço ou um serviço AWS) está indisponível e não
 * existe um valor padrão seguro para retornar. Mapeada para HTTP 503.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
