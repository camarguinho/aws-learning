package br.com.camarguinho.awslearning.common.exception;

import java.time.Instant;

/**
 * Corpo de erro padronizado devolvido por todas as APIs do case, seguindo um
 * formato simples e previsível para facilitar o consumo pelo front-end e a
 * documentação no Swagger/OpenAPI.
 *
 * @param timestamp momento em que o erro ocorreu
 * @param status    código HTTP retornado
 * @param error     nome curto do erro (ex.: "Not Found")
 * @param message   mensagem legível para humanos
 * @param path      caminho da requisição que originou o erro
 */
public record ApiError(Instant timestamp, int status, String error, String message, String path) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(Instant.now(), status, error, message, path);
    }
}
