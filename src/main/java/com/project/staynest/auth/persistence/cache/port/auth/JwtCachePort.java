package com.project.staynest.auth.persistence.cache.port.auth;

import com.project.staynest.auth.persistence.cache.redis.model.jwt.JwtCacheData;
import reactor.core.publisher.Mono;

public interface JwtCachePort {
    Mono<Void> saveJwtCache(JwtCacheData jwtCacheData);
}
