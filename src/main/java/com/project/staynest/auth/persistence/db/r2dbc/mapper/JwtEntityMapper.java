package com.project.staynest.auth.persistence.db.r2dbc.mapper;

import com.project.staynest.auth.persistence.db.r2dbc.entity.JwtEntity;
import com.project.staynest.auth.persistence.db.r2dbc.model.JwtTokenData;

public class JwtEntityMapper {
    private JwtEntityMapper(){}

    public static JwtEntity from(JwtTokenData jwtTokenData){
        JwtEntity jwtEntity = new JwtEntity();

        jwtEntity.setSubject(jwtTokenData.subject());
        jwtEntity.setAccessJwtId(jwtTokenData.accessJwtTokenId());
        jwtEntity.setRefreshJwtId(jwtTokenData.refreshJwtTokenId());
        jwtEntity.setSessionId(jwtTokenData.sessionId());
        jwtEntity.setRefreshExpiresAt(jwtTokenData.refreshExpirationTime());
        jwtEntity.setStatus(jwtTokenData.status());
        jwtEntity.setRole(jwtTokenData.role());
        jwtEntity.setDeviceId(jwtTokenData.deviceId());
        jwtEntity.setUserAgent(jwtTokenData.userAgent());

        return jwtEntity;

    }
}
