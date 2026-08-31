package com.project.staynest.auth.persistence.db.port;

import com.project.staynest.auth.persistence.db.r2dbc.model.UserData;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SignupRepositoryPort {
    Mono<Void> saveUserData(UserData userData);
    Mono<Boolean> checkPublicIdIndexExists(List<byte[]> publicIdIndexes);
    Mono<Boolean> checkUsernameIndexExists(List<byte[]> usernameIndexes);
    Mono<Boolean> checkEmailIndexExists(List<byte[]> emailIndexes);
}