package com.project.staynest.auth.persistence.db.r2dbc.jooq.impl;

import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import com.project.staynest.auth.persistence.db.r2dbc.jooq.repository.JwtJooqRepository;
import com.project.staynest.auth.persistence.db.r2dbc.model.JwtRotationData;
import com.project.staynest.auth.validation.Validation;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import com.project.staynest.auth.jooq.tables.Jwt;

import java.time.ZoneOffset;

@Repository
public class JwtJooqRepositoryImpl implements JwtJooqRepository {

    private static final Jwt JWT_ENTITY = Jwt.JWT;
    private final String CLASS_NAME = this.getClass().getSimpleName();

    private final DSLContext dsl;
    public JwtJooqRepositoryImpl(
            DSLContext dsl
    ){
        this.dsl = dsl;
    }

    @Override
    public Mono<Void> rotateJwt(JwtRotationData jwtRotationData) {

        return Mono.defer(()->{
            Validation.validate(jwtRotationData, "jwtRotationData", CLASS_NAME);

            long expectedRows = 1;

            return Mono.from(
                    this.dsl
                            .update(JWT_ENTITY)
                            .set(JWT_ENTITY.ACCESS_JWT_ID, jwtRotationData.accessJwtTokenId())
                            .set(JWT_ENTITY.REFRESH_JWT_ID, jwtRotationData.refreshJwtTokenId())
                            .set(
                                    JWT_ENTITY.REFRESH_EXPIRES_AT,
                                    jwtRotationData.refreshExpirationTime()
                                            .atZone(ZoneOffset.UTC)
                                            .toLocalDateTime()
                            )
                            .where(
                                    JWT_ENTITY.SUBJECT.eq(jwtRotationData.subject())
                                            .and(JWT_ENTITY.SESSION_ID.eq(jwtRotationData.sessionId()))
                            )
            )
                    .switchIfEmpty(Mono.error(new UnexpectedIllegalStateException(
                                    "MySQL returned null instead of updated rows while updating users_lookup crypto " + CLASS_NAME
                            ))
                    )
                    .flatMap(updatedRows->{
                        if(updatedRows == 0){
                            //no rows updated
                            return Mono.empty();
                        }

                        if(updatedRows != expectedRows){
                            throw new IllegalStateException(
                                    "Multiple jwt rows updated in " + CLASS_NAME
                            );
                        }

                        return Mono.empty();
                    });
        });

    }
}
