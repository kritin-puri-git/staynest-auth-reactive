package com.project.staynest.auth.persistence.db.r2dbc.model;

import com.project.staynest.auth.validation.Validation;

public record UsersCryptoRotationData(
        Long userLookupId,
        UsersRotationData usersRotationData,
        UsersLookupRotationData usersLookupRotationData
) {
    public UsersCryptoRotationData{
        Validation.validate(userLookupId, "userLookupId", this.getClass().getSimpleName());
    }
}
