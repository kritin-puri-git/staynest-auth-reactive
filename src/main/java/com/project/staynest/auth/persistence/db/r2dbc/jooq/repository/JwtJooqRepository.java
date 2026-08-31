package com.project.staynest.auth.persistence.db.r2dbc.jooq.repository;


import com.project.staynest.auth.persistence.db.r2dbc.model.JwtRotationData;
import reactor.core.publisher.Mono;

public interface JwtJooqRepository {

    Mono<Void> rotateJwt(JwtRotationData jwtRotationData);
}
