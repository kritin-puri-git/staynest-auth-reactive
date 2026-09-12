package com.project.staynest.auth.persistence.db.r2dbc.adapter;

import com.project.staynest.auth.persistence.db.r2dbc.entity.UsersEntity;
import com.project.staynest.auth.persistence.db.r2dbc.entity.UsersLookupEntity;
import com.project.staynest.auth.persistence.db.r2dbc.mapper.UsersEntityMapper;
import com.project.staynest.auth.persistence.db.r2dbc.mapper.UsersLookupEntityMapper;
import com.project.staynest.auth.persistence.db.r2dbc.model.UserData;
import com.project.staynest.auth.persistence.db.r2dbc.repository.UsersLookupRepository;
import com.project.staynest.auth.persistence.db.r2dbc.repository.UsersRepository;
import com.project.staynest.auth.persistence.db.port.SignupRepositoryPort;
import com.project.staynest.auth.validation.Validation;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class MysqlSignupRepositoryAdapter implements SignupRepositoryPort {

    private final String CLASS_NAME = this.getClass().getSimpleName();
    private final UsersRepository usersRepository;
    private final UsersLookupRepository usersLookupRepository;
    public MysqlSignupRepositoryAdapter(
            UsersRepository usersRepository,
            UsersLookupRepository usersLookupRepository
    ){
        this.usersRepository = usersRepository;
        this.usersLookupRepository = usersLookupRepository;
    }

    @Override
    public Mono<Void> saveUserData(UserData userData) {

        return Mono.defer(()->{
            Validation.validate(userData, "userData", CLASS_NAME);

            UsersLookupEntity usersLookupEntity = UsersLookupEntityMapper.from(
                    userData
            );

            return this.usersLookupRepository.save(usersLookupEntity)
                    .flatMap(savedLookup ->{
                        UsersEntity usersEntity = UsersEntityMapper.from(
                                savedLookup.getId(),
                                userData
                        );

                        usersEntity.setIsNew(true);

                        return this.usersRepository.save(usersEntity)
                                .flatMap(usersEntity1 -> {
                                    usersEntity.setIsNew(false);
                                    return Mono.empty();
                                })
                                ;

                    });
        });
    }

    @Override
    public Mono<Boolean> checkPublicIdIndexExists(List<byte[]> publicIdIndexes) {
        return Mono.defer(()->{
            Validation.validate(publicIdIndexes, "publicIdIndexes", CLASS_NAME);
            return this.usersLookupRepository.existsByPublicIdIndexIn(
                    publicIdIndexes
            );
        });
    }

    @Override
    public Mono<Boolean> checkUsernameIndexExists(List<byte[]> usernameIndexes) {
        return Mono.defer(()->{
            Validation.validate(usernameIndexes, "usernameIndexes", CLASS_NAME);
            return this.usersLookupRepository.existsByUsernameIndexIn(
                    usernameIndexes
            );
        });
    }

    @Override
    public Mono<Boolean> checkEmailIndexExists(List<byte[]> emailIndexes) {

        return Mono.defer(()->{
            Validation.validate(emailIndexes, "emailIndexes", CLASS_NAME);
            return this.usersLookupRepository.existsByEmailIndexIn(
                    emailIndexes
            );
        });
    }


}
