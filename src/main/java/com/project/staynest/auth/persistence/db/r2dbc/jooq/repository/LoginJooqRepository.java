package com.project.staynest.auth.persistence.db.r2dbc.jooq.repository;

import com.project.staynest.auth.persistence.db.r2dbc.model.EmailIndexData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersCryptoRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersLookupRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserDataProjection;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserEmailProjection;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface LoginJooqRepository {
    Flux<UserEmailProjection> findUsersEmail(List<EmailIndexData> emailIndexData);
    Flux<UserDataProjection> findUsersData(List<EmailIndexData> emailIndexData);
    Mono<Void> rotateUsersEntity(Long userLookupId, UsersRotationData usersRotationData);
    Mono<Void> rotateUsersLookupEntity(Long id, UsersLookupRotationData usersLookupRotationData);
    Mono<Void> rotateUsersAndUsersLookupEntity(UsersCryptoRotationData usersCryptoRotationData);
}
