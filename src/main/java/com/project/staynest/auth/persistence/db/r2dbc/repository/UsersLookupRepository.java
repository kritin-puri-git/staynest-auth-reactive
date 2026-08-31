package com.project.staynest.auth.persistence.db.r2dbc.repository;

import com.project.staynest.auth.persistence.db.r2dbc.entity.UsersLookupEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.Collection;


public interface UsersLookupRepository
        extends ReactiveCrudRepository<UsersLookupEntity, Long> {

    Mono<Boolean> existsByPublicIdIndexIn(Collection<byte[]> publicIdIndexes);
    Mono<Boolean> existsByUsernameIndexIn(Collection<byte[]> usernameIndexes);
    Mono<Boolean> existsByEmailIndexIn(Collection<byte[]> emailIndexes);

}
