package com.project.staynest.auth.persistence.db.port;

import com.project.staynest.auth.persistence.db.r2dbc.model.JwtRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.JwtTokenData;
import reactor.core.publisher.Mono;

public interface JwtRepositoryPort {
    Mono<Void> saveJwtData(JwtTokenData jwtTokenData);
    Mono<Void> rotateJwt(JwtRotationData jwtRotationData);
}
