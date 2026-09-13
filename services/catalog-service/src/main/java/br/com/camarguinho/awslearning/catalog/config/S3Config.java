package br.com.camarguinho.awslearning.catalog.config;

import io.awspring.cloud.autoconfigure.core.AwsClientBuilderConfigurer;
import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails;
import io.awspring.cloud.autoconfigure.s3.properties.S3Properties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Presigner de S3 usado só pra assinar as URLs devolvidas ao cliente
 * (upload/download direto de imagem de produto — ver {@code
 * ProductImageService}), com endpoint independente do {@code S3Client}
 * "normal" da aplicação.
 *
 * <p>Contra o MiniStack isso importa porque são dois consumidores em redes
 * diferentes: o {@code S3Client} roda dentro do container do
 * catalog-service e precisa do hostname interno do Docker
 * ({@code http://ministack:4566}, configurado em {@code
 * spring.cloud.aws.s3.endpoint}); já a URL assinada é aberta pelo browser/
 * cliente HTTP do desenvolvedor, rodando no host, onde esse hostname não
 * resolve. {@code app.s3.public-endpoint} (só setado no profile local)
 * fornece o host alcançável de fora (`http://localhost:4566`) só pra esse
 * segundo caso. Sem essa property (perfil de produção), esse bean nem é
 * criado e o {@code S3Presigner} default do Spring Cloud AWS assume o
 * endpoint real da AWS normalmente.</p>
 */
@Configuration
public class S3Config {

    @Bean
    @ConditionalOnProperty("app.s3.public-endpoint")
    public S3Presigner s3Presigner(S3Properties properties,
                                    AwsCredentialsProvider credentialsProvider,
                                    AwsRegionProvider regionProvider,
                                    ObjectProvider<AwsConnectionDetails> connectionDetails,
                                    @Value("${app.s3.public-endpoint}") URI publicEndpoint) {
        return S3Presigner.builder()
                .serviceConfiguration(properties.toS3Configuration())
                .credentialsProvider(credentialsProvider)
                .region(AwsClientBuilderConfigurer.resolveRegion(properties, connectionDetails.getIfAvailable(), regionProvider))
                .endpointOverride(publicEndpoint)
                .build();
    }
}
