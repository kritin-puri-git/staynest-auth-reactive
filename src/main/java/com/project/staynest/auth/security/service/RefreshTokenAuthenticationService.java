package com.project.staynest.auth.security.service;

import com.project.staynest.auth.security.model.JwtIdentity;
import com.project.staynest.auth.security.model.RefreshContext;
import reactor.core.publisher.Mono;

public interface RefreshTokenAuthenticationService {

    Mono<JwtIdentity> authenticate(RefreshContext refreshContext);
}
