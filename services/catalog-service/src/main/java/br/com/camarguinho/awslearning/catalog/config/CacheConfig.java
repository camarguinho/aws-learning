package br.com.camarguinho.awslearning.catalog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Configura o {@code RedisCacheManager} usado pela camada {@code @Cacheable}
 * de {@link br.com.camarguinho.awslearning.catalog.service.ProductService}.
 *
 * <p>O TTL do cache é lido de uma property (na prática, alimentada pelo AWS
 * Systems Manager Parameter Store — veja
 * {@code /ecommerce/catalog-service/cache-ttl-seconds} no
 * {@code application.yml}) para permitir ajustar o tempo de vida do cache sem
 * novo deploy.</p>
 */
@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            @Value("${app.cache.products-ttl-seconds}") long ttlSeconds) {

        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // GenericJackson2JsonRedisSerializer só ativa o "@class" no JSON (info
        // de tipo necessária pra desserializar de volta pro DTO correto) por
        // conta própria quando NÃO recebe um ObjectMapper customizado. Como
        // fornecemos o nosso (pra desligar WRITE_DATES_AS_TIMESTAMPS), é
        // preciso pedir isso explicitamente com defaultTyping(true) — do
        // contrário o cache grava JSON sem tipo e a leitura de volta vira um
        // LinkedHashMap genérico, quebrando o cast pro tipo esperado.
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(GenericJackson2JsonRedisSerializer.builder()
                                .objectMapper(objectMapper)
                                .defaultTyping(true)
                                .build()));

        return builder -> builder.cacheDefaults(configuration);
    }
}
