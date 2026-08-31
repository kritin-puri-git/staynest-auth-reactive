package com.project.staynest.auth.persistence.db.r2dbc.repository;

import com.project.staynest.auth.persistence.db.r2dbc.entity.UsersEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;


public interface UsersRepository
        extends ReactiveCrudRepository<UsersEntity, Long> {
}
