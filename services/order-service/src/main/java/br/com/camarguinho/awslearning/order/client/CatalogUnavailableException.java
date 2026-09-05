package br.com.camarguinho.awslearning.order.client;

/** Lançada pelo fallback de {@link CatalogClient} quando o circuito está aberto. */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
