package com.project.staynest.auth.business.service.impl;

import com.project.staynest.auth.business.model.LatestTokenData;
import com.project.staynest.auth.business.model.jwt.JwtGenerationResult;
import com.project.staynest.auth.business.model.jwt.JwtUserData;
import com.project.staynest.auth.business.service.JwtTokenService;
import com.project.staynest.auth.constants.JwtConstants;
import com.project.staynest.auth.enums.JwtRole;
import com.project.staynest.auth.enums.UserStatus;
import com.project.staynest.auth.jwt.model.TokenClaims;
import com.project.staynest.auth.jwt.service.JwtService;
import com.project.staynest.auth.persistence.cache.port.auth.JwtCachePort;
import com.project.staynest.auth.persistence.cache.redis.model.jwt.JwtCacheData;
import com.project.staynest.auth.persistence.db.port.JwtRepositoryPort;
import com.project.staynest.auth.persistence.db.r2dbc.model.JwtRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.JwtTokenData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    private static final String JWT_ACCESS_TOKEN_TYPE = JwtConstants.JWT_ACCESS_TOKEN_TYPE;
    private static final String JWT_REFRESH_TOKEN_TYPE = JwtConstants.JWT_REFRESH_TOKEN_TYPE;

    private static final long JWT_ACCESS_TOKEN_EXPIRY_IN_SECONDS = JwtConstants.JWT_ACCESS_TOKEN_EXPIRY_IN_SECONDS;
    private static final long JWT_REFRESH_TOKEN_EXPIRY_IN_SECONDS = JwtConstants.JWT_REFRESH_TOKEN_EXPIRY_IN_SECONDS;

    private final JwtService jwtService;
    private final JwtRepositoryPort jwtRepositoryPort;
    private final JwtCachePort jwtCachePort;
    public JwtTokenServiceImpl(
            JwtService jwtService,
            JwtRepositoryPort jwtRepositoryPort,
            JwtCachePort jwtCachePort
    ){
        this.jwtService = jwtService;
        this.jwtRepositoryPort = jwtRepositoryPort;
        this.jwtCachePort = jwtCachePort;
    }

    @Transactional
    @Override
    public Mono<JwtGenerationResult> generateUserTokenDetails(
            String subject,
            UserStatus status,
            String deviceId,
            String userAgent
    ) {

        return Mono.defer(()->{

            final String sessionId = UUID.randomUUID().toString();
            final String accessTokenJwtId = UUID.randomUUID().toString();
            final String refreshTokenJwtId = UUID.randomUUID().toString();

            final JwtRole role = JwtRole.USER;
            final List<String> audience = role.getAudience();

            final Instant now = Instant.now();
            final Date issueTime = Date.from(now);
            final Instant refreshExpirationTime = now.plusSeconds(
                    JWT_REFRESH_TOKEN_EXPIRY_IN_SECONDS
            );

            JwtGenerationResult jwtGenerationResult =
                    getJwtTokens(
                            subject,
                            accessTokenJwtId,
                            refreshTokenJwtId,
                            sessionId,
                            role,
                            audience,
                            now,
                            issueTime,
                            refreshExpirationTime,
                            deviceId
                    );

            return saveJwtDetails(
                    new JwtTokenData(
                            subject,
                            accessTokenJwtId,
                            refreshTokenJwtId,
                            sessionId,
                            refreshExpirationTime,
                            status.name(),
                            role.name(),
                            deviceId,
                            userAgent
                    )
            )
                    .then(
                            saveAccessTokenCache(
                                    new JwtCacheData(
                                            subject,
                                            sessionId,
                                            accessTokenJwtId,
                                            status.name(),
                                            deviceId,
                                            userAgent,
                                            JWT_ACCESS_TOKEN_EXPIRY_IN_SECONDS
                                    )
                            )
                    )
                    .thenReturn(jwtGenerationResult);
        });

    }

    @Transactional
    @Override
    public Mono<LatestTokenData> rotateJwt(JwtUserData tokenData) {

        return Mono.defer(()->{
            final String accessTokenJwtId = UUID.randomUUID().toString();
            final String refreshTokenJwtId = UUID.randomUUID().toString();

            final JwtRole jwtRole = JwtRole.from(tokenData.role());
            final UserStatus jwtStatus = UserStatus.from(tokenData.status());

            final Instant now = Instant.now();
            final Date issueTime = Date.from(now);
            final Instant refreshExpirationTime = now.plusSeconds(
                    JWT_REFRESH_TOKEN_EXPIRY_IN_SECONDS
            );

            JwtGenerationResult jwtGenerationResult =
                    getJwtTokens(
                            tokenData.subject(),
                            accessTokenJwtId,
                            refreshTokenJwtId,
                            tokenData.sessionId(),
                            jwtRole,
                            tokenData.audience(),
                            now,
                            issueTime,
                            refreshExpirationTime,
                            tokenData.deviceId()
                    );

            return rotateJwtDetails(
                    tokenData.subject(),
                    tokenData.sessionId(),
                    accessTokenJwtId,
                    refreshTokenJwtId,
                    refreshExpirationTime
            )
                    .then(
                            saveAccessTokenCache(
                                    new JwtCacheData(
                                            tokenData.subject(),
                                            tokenData.sessionId(),
                                            accessTokenJwtId,
                                            jwtStatus.name(),
                                            tokenData.deviceId(),
                                            tokenData.userAgent(),
                                            JWT_ACCESS_TOKEN_EXPIRY_IN_SECONDS
                                    )
                            )
                    )
                    .thenReturn(
                            new LatestTokenData(
                                    jwtGenerationResult,
                                    tokenData.subject()
                            )
                    );
        });


    }

    private Mono<Void> rotateJwtDetails(
            String subject,
            String sessionId,
            String accessJwtTokenId,
            String refreshJwtTokenId,
            Instant refreshExpirationTime
    ){
        return this.jwtRepositoryPort.rotateJwt(
                new JwtRotationData(
                        subject,
                        sessionId,
                        accessJwtTokenId,
                        refreshJwtTokenId,
                        refreshExpirationTime
                )
        );
    }

    private Mono<Void> saveAccessTokenCache(JwtCacheData jwtCacheDetails){
        return this.jwtCachePort.saveJwtCache(
                jwtCacheDetails
        );
    }

    private Mono<Void> saveJwtDetails(JwtTokenData jwtTokenDetails){
        return this.jwtRepositoryPort.saveJwtData(
                jwtTokenDetails
        );
    }

    private JwtGenerationResult getJwtTokens(
            String subject,
            String accessTokenJwtId,
            String refreshTokenJwtId,
            String sessionId,
            JwtRole role,
            List<String> audience,
            Instant now,
            Date issueTime,
            Instant refreshExpirationTime,
            String deviceId
    ){
        String accessToken = getAccessToken(
                subject,
                accessTokenJwtId,
                sessionId,
                role,
                audience,
                now,
                issueTime
        );

        String refreshToken = getRefreshToken(
                subject,
                refreshTokenJwtId,
                sessionId,
                role,
                audience,
                issueTime,
                refreshExpirationTime
        );

        return new JwtGenerationResult(
                accessToken,
                refreshToken,
                deviceId,
                JWT_REFRESH_TOKEN_EXPIRY_IN_SECONDS
        );

    }

    private String getAccessToken(
            String subject,
            String accessTokenJwtId,
            String sessionId,
            JwtRole role,
            List<String> audience,
            Instant now,
            Date issueTime
    ){

        final Date accessExpirationTime = Date.from(
                now.plusSeconds(
                        JwtConstants.JWT_ACCESS_TOKEN_EXPIRY_IN_SECONDS
                )
        );

        TokenClaims accessTokenClaimsDetail =
                new TokenClaims(
                        accessTokenJwtId,
                        subject,
                        sessionId,
                        audience,
                        role.name(),
                        JWT_ACCESS_TOKEN_TYPE,
                        issueTime,
                        accessExpirationTime
                );

        return this.jwtService.generateToken(
                accessTokenClaimsDetail
        );
    }

    private String getRefreshToken(
            String subject,
            String refreshTokenJwtId,
            String sessionId,
            JwtRole role,
            List<String> audience,
            Date issueTime,
            Instant refreshExpirationTime
    ){

        TokenClaims refreshTokenClaimsDetail =
                new TokenClaims(
                        refreshTokenJwtId,
                        subject,
                        sessionId,
                        audience,
                        role.name(),
                        JWT_REFRESH_TOKEN_TYPE,
                        issueTime,
                        Date.from(refreshExpirationTime)
                );

        return this.jwtService.generateToken(
                refreshTokenClaimsDetail
        );
    }
}
