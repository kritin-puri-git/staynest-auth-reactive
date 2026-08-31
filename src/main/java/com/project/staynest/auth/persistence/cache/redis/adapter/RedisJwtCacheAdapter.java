package com.project.staynest.auth.persistence.cache.redis.adapter;

import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import com.project.staynest.auth.persistence.cache.port.auth.JwtCachePort;
import com.project.staynest.auth.persistence.cache.redis.keys.RedisKeys;
import com.project.staynest.auth.persistence.cache.redis.mapper.jwt.JwtCacheMapper;
import com.project.staynest.auth.persistence.cache.redis.model.jwt.JwtCache;
import com.project.staynest.auth.persistence.cache.redis.model.jwt.JwtCacheData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class RedisJwtCacheAdapter implements JwtCachePort {

    public final String CLASS_NAME = this.getClass().getSimpleName();

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    public RedisJwtCacheAdapter(
            @Qualifier("masterReactiveRedisTemplate")
            ReactiveRedisTemplate<String, Object> reactiveRedisTemplate
    ){
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    @Override
    public Mono<Void> saveJwtCache(JwtCacheData jwtCacheData) {

        return Mono.defer(()->{
            String key = RedisKeys.getJwtSessionKey(jwtCacheData.subject(), jwtCacheData.sessionId());

            JwtCache jwtCache = JwtCacheMapper.from(jwtCacheData);

            return this.reactiveRedisTemplate.opsForValue()
                    .set(
                            key,
                            jwtCache,
                            Duration.ofSeconds(jwtCacheData.ttl())
                    )
                    .flatMap(saved->{
                        if(!saved)
                             return Mono.error(
                                     new UnexpectedIllegalStateException(
                                             "Jwt Cache failed to store.[" + CLASS_NAME + "]"
                                     )
                             );

                        return Mono.empty();
                    });
        });
    }
}
