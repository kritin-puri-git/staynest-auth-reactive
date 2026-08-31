package com.project.staynest.auth.persistence.db.r2dbc.mapper;

import com.project.staynest.auth.persistence.db.r2dbc.entity.UsersEntity;
import com.project.staynest.auth.persistence.db.r2dbc.model.UserData;

public final class UsersEntityMapper {
    private UsersEntityMapper(){}

    public static UsersEntity from(UserData userData){
        return UsersEntity
                .builder()
                .publicId(userData.encryptedPublicId())
                .username(userData.encryptedUsername())
                .email(userData.encryptedEmail())
                .encryptionKeyId(userData.encryptionKeyId())
                .encryptionVersion(userData.encryptionVersion())
                .status(userData.status())
                .build();
    }
}
