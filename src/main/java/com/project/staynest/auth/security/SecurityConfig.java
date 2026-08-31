package com.project.staynest.auth.security;

import com.project.staynest.auth.security.filter.RefreshTokenWebFilter;
import com.project.staynest.auth.security.managers.RefreshTokenAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http,
            RefreshTokenWebFilter refreshTokenWebFilter
    )throws Exception{

        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable);
        http.httpBasic(Customizer.withDefaults());

        http.csrf(
                csrfSpec ->
                        csrfSpec.csrfTokenRepository(
                                new CookieServerCsrfTokenRepository()
                        )
        );

        http.addFilterAfter(
                refreshTokenWebFilter,
                SecurityWebFiltersOrder.SECURITY_CONTEXT_SERVER_WEB_EXCHANGE
        );

        http.authorizeExchange(
                exchange->
                        exchange.pathMatchers(
                                "/v1/auth/sign-up",
                                "/v1/auth/email-login",
                                "/v1/auth/refresh"
                        ).permitAll()
                                .anyExchange()
                                .authenticated()
        );

        return http.build();
    }


    @Bean
    public ReactiveAuthenticationManager authenticationManager(
            RefreshTokenAuthenticationManager refreshTokenAuthenticationManager
    ){
        return new DelegatingReactiveAuthenticationManager(
                refreshTokenAuthenticationManager
        );
    }

}