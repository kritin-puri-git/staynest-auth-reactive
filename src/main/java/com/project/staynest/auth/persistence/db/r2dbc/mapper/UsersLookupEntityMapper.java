package com.project.staynest.auth.persistence.db.r2dbc.mapper;

import com.project.staynest.auth.persistence.db.r2dbc.entity.UsersLookupEntity;
import com.project.staynest.auth.persistence.db.r2dbc.model.UserData;

public final class UsersLookupEntityMapper {
    private UsersLookupEntityMapper(){}

    public static UsersLookupEntity from(UserData userData){

        return UsersLookupEntity
                .builder()
                .publicIdIndex(userData.publicIdHash())
                .usernameIndex(userData.usernameHash())
                .emailIndex(userData.emailHash())
                .hashingKeyId(userData.hashingKeyId())
                .hashingVersion(userData.hashingVersion())
                .build();
    }
}
