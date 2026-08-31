package com.project.staynest.auth.security.filter;

import com.project.staynest.auth.business.service.RateLimiterService;
import com.project.staynest.auth.constants.CookieConstants;
import com.project.staynest.auth.enums.UserAgentStatus;
import com.project.staynest.auth.security.authentication.RefreshTokenAuthentication;
import com.project.staynest.auth.security.model.RefreshContext;
import com.project.staynest.auth.validation.Validation;

import io.github.bucket4j.BucketConfiguration;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.InetSocketAddress;
import java.util.List;

@Component
public class RefreshTokenWebFilter implements WebFilter {

    private final static String IP_RATE_LIMITER_KEY = "rate-limit:refresh:ip:";
    private final static String DEVICE_ID_RATE_LIMITER_KEY = "rate-limit:refresh:device:";

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private final ReactiveAuthenticationManager authenticationManager;
    private final RateLimiterService rateLimiterService;
    private final BucketConfiguration refreshTokenIpBucketConfiguration;
    private final BucketConfiguration refreshTokenDeviceIdBucketConfiguration;
    public RefreshTokenWebFilter(
            ReactiveAuthenticationManager authenticationManager,
            RateLimiterService rateLimiterService,
            BucketConfiguration refreshTokenIpBucketConfiguration,
            BucketConfiguration refreshTokenDeviceIdBucketConfiguration
    ){
        this.authenticationManager = authenticationManager;
        this.rateLimiterService = rateLimiterService;
        this.refreshTokenIpBucketConfiguration = refreshTokenIpBucketConfiguration;
        this.refreshTokenDeviceIdBucketConfiguration = refreshTokenDeviceIdBucketConfiguration;
    }

    @Override
    public @NonNull Mono<Void> filter(
            @NonNull ServerWebExchange exchange,
            @NonNull WebFilterChain chain
    ) {

        return Mono.defer(()->{

            ServerHttpRequest request = exchange.getRequest();
            if(!request.getURI().getPath().equalsIgnoreCase("/v1/auth/refresh"))
                return chain.filter(exchange);

            String ip = getIp(request);
            Validation.validate(ip, "ip", CLASS_NAME);

            return tryConsume(
                    IP_RATE_LIMITER_KEY + ip,
                    this.refreshTokenIpBucketConfiguration
            )
                    .then(Mono.defer(()->{

                        RefreshContext refreshContext = getRefreshContext(request);

                        return tryConsume(
                                DEVICE_ID_RATE_LIMITER_KEY + refreshContext.deviceId(),
                                this.refreshTokenDeviceIdBucketConfiguration
                        )
                                .then(Mono.defer(()->{
                                    RefreshTokenAuthentication refreshAuthentication =
                                            new RefreshTokenAuthentication(refreshContext);

                                    return this.authenticationManager.authenticate(
                                                    refreshAuthentication
                                            )
                                            .flatMap(authentication ->{
                                                Context context =
                                                        ReactiveSecurityContextHolder.withAuthentication(
                                                                authentication
                                                        );
                                                return chain.filter(exchange).contextWrite(context);
                                            });
                                }));


                    }))
                    ;

        })
                .onErrorMap(IllegalArgumentException.class,
                        ex->
                                new BadCredentialsException(
                                        "Invalid Cookie Credentials",
                                        ex
                                )
                )
                .onErrorResume(IllegalStateException.class, _ ->{
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                })
                ;
    }

    private RefreshContext getRefreshContext(ServerHttpRequest request){

        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        List<HttpCookie> deviceIdList = cookies.get(CookieConstants.DEVICE_ID_COOKIE_NAME);
        if(deviceIdList == null)
            throw new BadCredentialsException("DeviceId cookie not found");
        if(deviceIdList.size()!=1)
            throw new BadCredentialsException("Multiple DeviceId cookies found");
        String deviceId = deviceIdList.getFirst().getValue();

        List<HttpCookie> activeUserIdList = cookies.get(CookieConstants.ACTIVE_USER_PUBLIC_ID_COOKIE_NAME);
        if(activeUserIdList == null)
            throw new BadCredentialsException("ActiveUserId cookie not found");
        if(activeUserIdList.size()!=1)
            throw new BadCredentialsException("Multiple ActiveUserId cookies found");
        String activeUserId = activeUserIdList.getFirst().getValue();

        Validation.validate(deviceId, "deviceId", CLASS_NAME);
        Validation.validate(activeUserId, "activeUserId", CLASS_NAME);

        List<HttpCookie> refreshTokenList = cookies.get(CookieConstants.getRefreshTokenCookieName(activeUserId));
        if(refreshTokenList == null)
            throw new BadCredentialsException("RefreshToken cookie not found");
        if(refreshTokenList.size()!=1)
            throw new BadCredentialsException("Multiple RefreshToken cookies found");
        String refreshToken = refreshTokenList.getFirst().getValue();

        Validation.validate(refreshToken, "refreshToken", CLASS_NAME);

        HttpHeaders headers = request.getHeaders();

        List<String> userAgentList = headers.get(CookieConstants.CLIENT_USER_AGENT);

        if(userAgentList == null || userAgentList.isEmpty())
            userAgentList = List.of(UserAgentStatus.NOT_AVAILABLE.toString());

        String userAgent = userAgentList.getFirst();
        if(userAgent == null || userAgent.isBlank())
            userAgent = UserAgentStatus.NOT_AVAILABLE.toString();
        Validation.validate(userAgent, "userAgent", CLASS_NAME);

        return new RefreshContext(
                refreshToken,
                deviceId,
                userAgent
        );
    }


    private Mono<Void> tryConsume(
            String key,
            BucketConfiguration ipConfiguration
    ){
        return this.rateLimiterService.tryConsume(
                        key,
                        ipConfiguration
                )
                .flatMap(allowed->
                        allowed
                                ? Mono.empty()
                                :Mono.error(new IllegalStateException("Rate Limit Exceeded"))
                );
    }

    private String getIp(
            ServerHttpRequest request
    ){
        InetSocketAddress remoteAddress = request.getRemoteAddress();

        if (remoteAddress == null || remoteAddress.isUnresolved()
                || remoteAddress.getAddress() == null) {
            return null;
        }

        return remoteAddress.getAddress().getHostAddress();
    }

}