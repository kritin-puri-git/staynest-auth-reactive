package com.project.staynest.auth.config.redis.reactive;

import com.project.staynest.auth.config.redis.RedisProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ReactiveRedisConfig {
    private final RedisProperties redisProperties;
    public ReactiveRedisConfig(
            RedisProperties redisProperties
    ){
        this.redisProperties = redisProperties;
    }

    @Primary
    @Bean(name = "masterReactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory masterReactiveRedisConnectionFactory(){
        RedisProperties.Node master = redisProperties.getMaster();

        return createReactiveRedisConnectionFactory(master);
    }

    @Bean(name = "replicaReactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory replicaReactiveRedisConnectionFactory(){
        RedisProperties.Node replica = redisProperties.getReplica();

        return createReactiveRedisConnectionFactory(replica);
    }

    private ReactiveRedisConnectionFactory createReactiveRedisConnectionFactory(
            RedisProperties.Node redisPropertyNode
    ){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();

        configuration.setHostName(redisPropertyNode.getHost());
        configuration.setPort(redisPropertyNode.getPort());
        configuration.setPassword(redisPropertyNode.getPassword());

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            @Qualifier("masterReactiveRedisConnectionFactory")
            ReactiveRedisConnectionFactory connectionFactory
    ){
        return new ReactiveRedisTemplate<>(
                connectionFactory,
                createSerializationContext()
        );

    }

    @Bean(name = "masterReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, Object> masterReactiveRedisTemplate(
            @Qualifier("masterReactiveRedisConnectionFactory")
            ReactiveRedisConnectionFactory connectionFactory
    ){
        return new ReactiveRedisTemplate<>(
                connectionFactory,
                createSerializationContext()
        );

    }

    @Bean(name = "replicaReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, Object> replicaReactiveRedisTemplate(
            @Qualifier("replicaReactiveRedisConnectionFactory")
            ReactiveRedisConnectionFactory connectionFactory
    ){
        return new ReactiveRedisTemplate<>(
                connectionFactory,
                createSerializationContext()
        );

    }

    private RedisSerializationContext<String, Object> createSerializationContext(){
        ObjectMapper objectMapper = JsonMapper.builder()
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .build();

        GenericJacksonJsonRedisSerializer valueSerializer = new GenericJacksonJsonRedisSerializer(objectMapper);
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        return RedisSerializationContext
                .<String, Object>newSerializationContext(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();
    }



}
