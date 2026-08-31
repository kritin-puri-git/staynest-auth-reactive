package com.project.staynest.auth.persistence.db.r2dbc.adapter;

import com.project.staynest.auth.persistence.db.port.RefreshTokenAuthenticationPort;
import com.project.staynest.auth.persistence.db.r2dbc.mapper.RefreshTokenDataMapper;
import com.project.staynest.auth.persistence.db.r2dbc.model.RefreshTokenData;
import com.project.staynest.auth.persistence.db.r2dbc.model.TokenIdentifier;
import com.project.staynest.auth.persistence.db.r2dbc.repository.JwtRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


@Repository
public class MysqlRefreshTokenAuthenticationAdapter implements RefreshTokenAuthenticationPort {

    private final JwtRepository jwtRepository;
    public MysqlRefreshTokenAuthenticationAdapter(
            JwtRepository jwtRepository
    ){
        this.jwtRepository = jwtRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public Mono<RefreshTokenData> getRefreshTokenData(TokenIdentifier tokenIdentifier) {

        return this.jwtRepository.findBySubjectAndSessionId(
                tokenIdentifier.subject(),
                tokenIdentifier.sessionId()
        )
                .map(RefreshTokenDataMapper::from);
    }
}
