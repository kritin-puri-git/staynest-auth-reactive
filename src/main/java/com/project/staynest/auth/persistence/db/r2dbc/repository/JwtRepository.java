package com.project.staynest.auth.persistence.db.r2dbc.repository;


import com.project.staynest.auth.persistence.db.r2dbc.entity.JwtEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface JwtRepository
        extends ReactiveCrudRepository<JwtEntity, Long> {
    Mono<JwtEntity> findBySubjectAndSessionId(String subject, String sessionId);
}
