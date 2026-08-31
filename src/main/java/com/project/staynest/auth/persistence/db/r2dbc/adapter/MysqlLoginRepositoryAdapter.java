package com.project.staynest.auth.persistence.db.r2dbc.adapter;

import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import com.project.staynest.auth.persistence.db.r2dbc.jooq.repository.LoginJooqRepository;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersCryptoRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.EmailIndexData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersLookupRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserDataProjection;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserEmailProjection;
import com.project.staynest.auth.persistence.db.port.LoginRepositoryPort;
import com.project.staynest.auth.validation.Validation;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class MysqlLoginRepositoryAdapter implements LoginRepositoryPort {

    private final String CLASS_NAME = this.getClass().getSimpleName();

    private final LoginJooqRepository loginJooqRepository;

    public MysqlLoginRepositoryAdapter(
            LoginJooqRepository loginJooqRepository
    ){
        this.loginJooqRepository = loginJooqRepository;
    }

    @Override
    public Flux<UserEmailProjection> getUsersEmail(List<EmailIndexData> emailIndexData) {

        return Flux.defer(()->{
            Validation.validate(emailIndexData, "emailIndexData", CLASS_NAME);

            return this.loginJooqRepository.findUsersEmail(
                    emailIndexData
            );
        });
    }

    @Override
    public Flux<UserDataProjection> getUsersData(List<EmailIndexData> emailIndexData) {

        return Flux.defer(()->{
            Validation.validate(emailIndexData, "emailIndexData", CLASS_NAME);

            return this.loginJooqRepository.findUsersData(
                    emailIndexData
            );
        });
    }

    @Override
    public Mono<Void> rotateUsersCrypto(UsersCryptoRotationData usersCryptoRotationData) {

        return Mono.defer(()->{
            Validation.validate(
                    usersCryptoRotationData,
                    "usersCryptoRotationData",
                    CLASS_NAME
            );
            Validation.validate(
                    usersCryptoRotationData.userLookupId(),
                    "userLookupId",
                    CLASS_NAME
            );

            final UsersRotationData usersRotationData =
                    usersCryptoRotationData.usersRotationData();
            final UsersLookupRotationData usersLookupRotationData =
                    usersCryptoRotationData.usersLookupRotationData();

            if (usersRotationData != null
                    && usersLookupRotationData != null){

                return this.rotateUsersAndUsersLookupEntity(
                        usersCryptoRotationData
                );

            }

            if(usersRotationData != null){

                return this.rotateUsersEntity(
                        usersCryptoRotationData.userLookupId(),
                        usersRotationData
                );
            }

            if(usersLookupRotationData != null){

                return this.rotateUsersLookupEntity(
                        usersCryptoRotationData.userLookupId(),
                        usersLookupRotationData
                );
            }

            return Mono.error(new UnexpectedIllegalStateException(
                    "No crypto rotation data available in " + CLASS_NAME
            ));
        });
    }

    private Mono<Void> rotateUsersEntity(
            Long userLookupId,
            UsersRotationData usersRotationData
    ){

        return Mono.defer(()->{
            Validation.validate(usersRotationData, "usersRotationData", CLASS_NAME);

            return this.loginJooqRepository.rotateUsersEntity(
                    userLookupId,
                    usersRotationData
            );
        });
    }

    private Mono<Void> rotateUsersLookupEntity(
            Long userLookupId,
            UsersLookupRotationData usersLookupRotationData
    ){

        return Mono.defer(()->{
            Validation.validate(usersLookupRotationData, "usersLookupRotationData", CLASS_NAME);

            return this.loginJooqRepository.rotateUsersLookupEntity(
                    userLookupId,
                    usersLookupRotationData
            );
        });
    }

    private Mono<Void> rotateUsersAndUsersLookupEntity(
            UsersCryptoRotationData usersCryptoRotationData
    ){

        return Mono.defer(()->{
            Validation.validate(usersCryptoRotationData, "usersCryptoRotationData", CLASS_NAME);
            return this.loginJooqRepository.rotateUsersAndUsersLookupEntity(
                    usersCryptoRotationData
            );
        });
    }

}
