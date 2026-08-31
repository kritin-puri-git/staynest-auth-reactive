package com.project.staynest.auth.persistence.db.port;

import com.project.staynest.auth.persistence.db.r2dbc.model.RefreshTokenData;
import com.project.staynest.auth.persistence.db.r2dbc.model.TokenIdentifier;
import reactor.core.publisher.Mono;

public interface RefreshTokenAuthenticationPort {

    Mono<RefreshTokenData> getRefreshTokenData(TokenIdentifier tokenIdentifier);
}
