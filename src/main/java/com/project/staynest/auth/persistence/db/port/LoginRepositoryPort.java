package com.project.staynest.auth.persistence.db.port;

import com.project.staynest.auth.persistence.db.r2dbc.model.UsersCryptoRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.EmailIndexData;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserDataProjection;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserEmailProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoginRepositoryPort {
    Flux<UserEmailProjection> getUsersEmail(List<EmailIndexData> emailIndexData);
    Flux<UserDataProjection> getUsersData(List<EmailIndexData> emailIndexData);
    Mono<Void> rotateUsersCrypto(UsersCryptoRotationData usersCryptoRotationData);
}
