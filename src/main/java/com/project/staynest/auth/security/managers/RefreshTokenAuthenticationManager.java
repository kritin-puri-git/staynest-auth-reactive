package com.project.staynest.auth.security.managers;

import com.project.staynest.auth.security.authentication.RefreshTokenAuthentication;
import com.project.staynest.auth.security.model.RefreshContext;
import com.project.staynest.auth.security.service.RefreshTokenAuthenticationService;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RefreshTokenAuthenticationManager implements ReactiveAuthenticationManager {

    private final RefreshTokenAuthenticationService refreshAuthenticationService;
    public RefreshTokenAuthenticationManager(
            RefreshTokenAuthenticationService refreshAuthenticationService
    ){
        this.refreshAuthenticationService = refreshAuthenticationService;
    }


    @Override
    public @NonNull Mono<Authentication> authenticate(@NonNull Authentication authentication) {

        return Mono.defer(()->{
            final RefreshTokenAuthentication refreshAuthentication =
                    (RefreshTokenAuthentication) authentication;

            final RefreshContext refreshContext =
                    (RefreshContext) refreshAuthentication.getCredentials();

            return this.refreshAuthenticationService.authenticate(refreshContext)
                    .map(jwtIdentity ->{
                        refreshAuthentication.markAuthenticated(
                                jwtIdentity
                        );
                        return refreshAuthentication;
                    });

        });
    }
}