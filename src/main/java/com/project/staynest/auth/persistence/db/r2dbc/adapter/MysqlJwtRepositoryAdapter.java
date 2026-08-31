package com.project.staynest.auth.persistence.db.r2dbc.adapter;

import com.project.staynest.auth.persistence.db.port.JwtRepositoryPort;
import com.project.staynest.auth.persistence.db.r2dbc.entity.JwtEntity;
import com.project.staynest.auth.persistence.db.r2dbc.jooq.repository.JwtJooqRepository;
import com.project.staynest.auth.persistence.db.r2dbc.mapper.JwtEntityMapper;
import com.project.staynest.auth.persistence.db.r2dbc.model.JwtRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.JwtTokenData;
import com.project.staynest.auth.persistence.db.r2dbc.repository.JwtRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class MysqlJwtRepositoryAdapter implements JwtRepositoryPort {

    private final JwtRepository jwtRepository;
    private final JwtJooqRepository jwtJooqRepository;
    public MysqlJwtRepositoryAdapter(
            JwtRepository jwtRepository,
            JwtJooqRepository jwtJooqRepository
    ){
        this.jwtRepository = jwtRepository;
        this.jwtJooqRepository = jwtJooqRepository;
    }

    @Override
    public Mono<Void> saveJwtData(JwtTokenData jwtTokenData) {

        return Mono.defer(()->{
            JwtEntity jwtEntity = JwtEntityMapper.from(
                    jwtTokenData
            );

            return this.jwtRepository.save(jwtEntity)
                    .then();
        });
    }

    @Override
    public Mono<Void> rotateJwt(JwtRotationData jwtRotationData) {

        return this.jwtJooqRepository.rotateJwt(jwtRotationData);
    }
}