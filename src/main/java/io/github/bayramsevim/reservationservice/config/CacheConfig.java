package io.github.bayramsevim.reservationservice.config;

import io.github.bayramsevim.reservationservice.show.ShowResponse;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {


    @Bean
    RedisCacheManagerBuilderCustomizer cacheCustomizer(JsonMapper jsonMapper) {
        JavaType showListType = jsonMapper.getTypeFactory()
                .constructCollectionType(List.class, ShowResponse.class);

        var showListSerializer = new JacksonJsonRedisSerializer<>(jsonMapper, showListType);

        return builder -> builder.withCacheConfiguration("shows",
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(showListSerializer)));
    }
}
