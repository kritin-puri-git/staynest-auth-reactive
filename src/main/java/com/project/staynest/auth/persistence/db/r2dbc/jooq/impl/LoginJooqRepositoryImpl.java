package com.project.staynest.auth.persistence.db.r2dbc.jooq.impl;

import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import com.project.staynest.auth.persistence.db.r2dbc.jooq.repository.LoginJooqRepository;
import com.project.staynest.auth.persistence.db.r2dbc.model.EmailIndexData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersCryptoRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersLookupRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.model.UsersRotationData;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserDataProjection;
import com.project.staynest.auth.persistence.db.r2dbc.projection.UserEmailProjection;

import com.project.staynest.auth.validation.Validation;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.RowCountQuery;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.jooq.types.UShort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.project.staynest.auth.jooq.Tables.USERS;
import static com.project.staynest.auth.jooq.Tables.USERS_LOOKUP;

@Repository
public class LoginJooqRepositoryImpl implements LoginJooqRepository {

    private static final String ROTATE_USERS_AND_USERS_LOOKUP_SQL =  """
                UPDATE
                    users AS u
                INNER JOIN users_lookup AS ul
                ON u.lookup_id = ul.id
                SET
                    u.public_id = :encryptedPublicId,
                    u.username = :encryptedUsername,
                    u.email = :encryptedEmail,
                    u.encryption_key_id = :encryptionKeyId,
                    u.encryption_version = :encryptionVersion,
                    ul.public_id_index = :publicIdIndex,
                    ul.username_index = :usernameIndex,
                    ul.email_index = :emailIndex,
                    ul.hashing_key_id = :hashingKeyId,
                    ul.hashing_version = :hashingVersion
                WHERE
                    ul.id = :id
                    AND
                    (
                        u.encryption_key_id <> :encryptionKeyId
                        OR
                        u.encryption_version <> :encryptionVersion
                    )
                    AND
                    (
                        ul.hashing_key_id <> :hashingKeyId
                        OR
                        ul.hashing_version <> :hashingVersion
                    )
                """;

    private final String CLASS_NAME = this.getClass().getSimpleName();
    private final DSLContext dsl;
    public LoginJooqRepositoryImpl(
            DSLContext dsl
    ){
        this.dsl = dsl;
    }

    @Override
    public Flux<UserEmailProjection> findUsersEmail(List<EmailIndexData> emailIndexData) {
        return Flux.defer(()->{
            Validation.validate(emailIndexData, "emailIndexData", CLASS_NAME);

            Condition condition = getEmailLookupCondition(emailIndexData);

            return Flux.from(
                            this.dsl
                                    .select(
                                            USERS.EMAIL,
                                            USERS.ENCRYPTION_KEY_ID,
                                            USERS.ENCRYPTION_VERSION
                                    )
                                    .from(USERS_LOOKUP)
                                    .innerJoin(USERS)
                                    .on(USERS_LOOKUP.ID.eq(USERS.LOOKUP_ID))
                                    .where(condition)
                    )
                    .map(record ->
                            new UserEmailProjection(
                                    record.get(USERS.EMAIL),
                                    record.get(USERS.ENCRYPTION_KEY_ID).shortValue(),
                                    record.get(USERS.ENCRYPTION_VERSION).shortValue()
                            )
                    );
        });
    }

    @Override
    public Flux<UserDataProjection> findUsersData(List<EmailIndexData> emailIndexData) {
        return Flux.defer(()->{
            Validation.validate(emailIndexData, "emailIndexData", CLASS_NAME);
            Condition condition = getEmailLookupCondition(emailIndexData);

            return Flux.from(
                            dsl
                                    .select(
                                            USERS.LOOKUP_ID.as("userLookupId"),
                                            USERS.PUBLIC_ID.as("encryptedPublicId"),
                                            USERS.USERNAME.as("encryptedUsername"),
                                            USERS.EMAIL.as("encryptedEmail"),
                                            USERS.ENCRYPTION_KEY_ID.as("encryptionKeyId"),
                                            USERS.ENCRYPTION_VERSION.as("encryptionVersion"),
                                            USERS.STATUS.as("status"),
                                            USERS_LOOKUP.HASHING_KEY_ID.as("hashingKeyId"),
                                            USERS_LOOKUP.HASHING_VERSION.as("hashingVersion")
                                    )
                                    .from(USERS_LOOKUP)
                                    .innerJoin(USERS)
                                    .on(USERS_LOOKUP.ID.eq(USERS.LOOKUP_ID))
                                    .where(condition)
                    )
                    .map(record ->
                            new UserDataProjection(
                                    record.get(USERS.LOOKUP_ID).longValue(),
                                    record.get(USERS.PUBLIC_ID),
                                    record.get(USERS.USERNAME),
                                    record.get(USERS.EMAIL),
                                    record.get(USERS.ENCRYPTION_KEY_ID).shortValue(),
                                    record.get(USERS.ENCRYPTION_VERSION).shortValue(),
                                    record.get(USERS_LOOKUP.HASHING_KEY_ID).shortValue(),
                                    record.get(USERS_LOOKUP.HASHING_VERSION).shortValue(),
                                    record.get(USERS.STATUS)
                            )
                    );
        });
    }

    private Condition getEmailLookupCondition(
            List<EmailIndexData> emailIndexData
    ){
        Condition condition = DSL.falseCondition();

        for(EmailIndexData indexData : emailIndexData){
            condition = condition.or(
                    USERS_LOOKUP.EMAIL_INDEX.eq(indexData.emailIndex())
                            .and(
                                    USERS_LOOKUP.HASHING_KEY_ID.eq(
                                            UShort.valueOf(indexData.keyId())
                                    )
                            )
                            .and(
                                    USERS_LOOKUP.HASHING_VERSION.eq(
                                            UShort.valueOf(indexData.version())
                                    )
                            )
            );
        }

        return condition;
    }

    @Override
    public Mono<Void> rotateUsersEntity(Long userLookupId, UsersRotationData usersRotationData) {

        return Mono.defer(()->{
            Validation.validate(userLookupId, "userLookupId", CLASS_NAME);
            Validation.validate(usersRotationData, "usersRotationData", CLASS_NAME);

            Condition needsRotation = USERS.ENCRYPTION_KEY_ID.ne(
                            UShort.valueOf(usersRotationData.encryptionKeyId())
                    )
                    .or(USERS.ENCRYPTION_VERSION.ne(
                            UShort.valueOf(usersRotationData.encryptionVersion())
                    ));

            long expectedRows = 1;

            return Mono.from(
                    this.dsl.update(USERS)
                            .set(USERS.PUBLIC_ID, usersRotationData.encryptedPublicId())
                            .set(USERS.USERNAME, usersRotationData.encryptedUsername())
                            .set(USERS.EMAIL, usersRotationData.encryptedEmail())
                            .set(USERS.ENCRYPTION_KEY_ID, UShort.valueOf(usersRotationData.encryptionKeyId()))
                            .set(USERS.ENCRYPTION_VERSION, UShort.valueOf(usersRotationData.encryptionVersion()))
                            .where(
                                    USERS.LOOKUP_ID.eq(ULong.valueOf(userLookupId))
                                            .and( needsRotation)
                            )
            )
                    .switchIfEmpty(Mono.error(new UnexpectedIllegalStateException(
                                    "MySQL returned null instead of updated rows while updating users crypto " + CLASS_NAME
                            ))
                    )
                    .flatMap(updatedRows->{
                        if(updatedRows != expectedRows){
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Multiple users updated in " + CLASS_NAME
                            ));
                        }
                        return Mono.empty();
                    });
        });
    }

    @Override
    public Mono<Void> rotateUsersLookupEntity(Long id, UsersLookupRotationData usersLookupRotationData) {
        return Mono.defer(()->{
            Validation.validate(id, "id", CLASS_NAME);
            Validation.validate(usersLookupRotationData, "usersLookupRotationData", CLASS_NAME);

            Condition needsRotation = USERS_LOOKUP.HASHING_KEY_ID.ne(
                            UShort.valueOf(usersLookupRotationData.hashingKeyId())
                    )
                    .or(USERS_LOOKUP.HASHING_VERSION.ne(
                            UShort.valueOf(usersLookupRotationData.hashingVersion())
                    ));

            long expectedRows = 1;

            return Mono.from(
                    this.dsl.update(USERS_LOOKUP)
                            .set(USERS_LOOKUP.PUBLIC_ID_INDEX, usersLookupRotationData.hashedPublicId())
                            .set(USERS_LOOKUP.USERNAME_INDEX, usersLookupRotationData.hashedUsername())
                            .set(USERS_LOOKUP.EMAIL_INDEX, usersLookupRotationData.hashedEmail())
                            .set(USERS_LOOKUP.HASHING_KEY_ID, UShort.valueOf(usersLookupRotationData.hashingKeyId()))
                            .set(USERS_LOOKUP.HASHING_VERSION, UShort.valueOf(usersLookupRotationData.hashingVersion()))
                            .where(
                                    USERS_LOOKUP.ID.eq(ULong.valueOf(id))
                                            .and( needsRotation)
                            )
            )
                    .switchIfEmpty(Mono.error(new UnexpectedIllegalStateException(
                                    "MySQL returned null instead of updated rows while updating users_lookup crypto " + CLASS_NAME
                            ))
                    )
                    .flatMap(updatedRows->{
                        if(updatedRows != expectedRows){
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Multiple users_lookup updated in " + CLASS_NAME
                            ));
                        }
                        return Mono.empty();
                    });
        });
    }

    @Override
    public Mono<Void> rotateUsersAndUsersLookupEntity(UsersCryptoRotationData usersCryptoRotationData) {
        return Mono.defer(()->{
            Validation.validate(usersCryptoRotationData, "usersCryptoRotationData", CLASS_NAME);

            UsersRotationData usersRotationData = usersCryptoRotationData.usersRotationData();
            UsersLookupRotationData usersLookupRotationData = usersCryptoRotationData.usersLookupRotationData();

            Validation.validate(usersRotationData, "usersRotationData", CLASS_NAME);
            Validation.validate(usersLookupRotationData, "usersLookupRotationData", CLASS_NAME);

            long expectedRows = 2;

            RowCountQuery query = dsl.query(ROTATE_USERS_AND_USERS_LOOKUP_SQL);
            query.bind("id", usersCryptoRotationData.userLookupId());
            query.bind("encryptedPublicId", usersRotationData.encryptedPublicId());
            query.bind("encryptedUsername", usersRotationData.encryptedUsername());
            query.bind("encryptedEmail", usersRotationData.encryptedEmail());
            query.bind("encryptionKeyId", usersRotationData.encryptionKeyId());
            query.bind("encryptionVersion", usersRotationData.encryptionVersion());
            query.bind("publicIdIndex",usersLookupRotationData.hashedPublicId());
            query.bind("usernameIndex", usersLookupRotationData.hashedUsername());
            query.bind("emailIndex", usersLookupRotationData.hashedEmail());
            query.bind("hashingKeyId", usersLookupRotationData.hashingKeyId());
            query.bind("hashingVersion", usersLookupRotationData.hashingVersion());

            return Mono.from(query)
                    .switchIfEmpty(Mono.error(new UnexpectedIllegalStateException(
                                    "MySQL returned null instead of updated rows while updating users and users_lookup crypto " + CLASS_NAME
                            ))
                    )
                    .flatMap(updatedRows->{
                        if(updatedRows != expectedRows){
                            return Mono.error(new UnexpectedIllegalStateException(
                                    "Unexpected affected rows: " + updatedRows +
                                            " while updating users and users_lookup crypto in " + CLASS_NAME
                            ));
                        }

                        return Mono.empty();
                    });
        });
    }
}

/*
 * Equivalent SQL:
 *
 * SELECT
 *      user.identifier,
 *      user.encryption_key_id,
 *      user.encryption_version
 *
 * FROM users_lookup AS lookup
 *
 * INNER JOIN users AS user
 *      ON lookup.userLookupId = user.lookup_id
 *
 * WHERE
 *      (
 *          lookup.email_index = ?
 *          AND lookup.hashing_key_id = ?
 *          AND lookup.hashing_version = ?
 *      )
 *      OR
 *      (
 *          lookup.email_index = ?
 *          AND lookup.hashing_key_id = ?
 *          AND lookup.hashing_version = ?
 *      )
 *      OR
 *      (
 *          lookup.email_index = ?
 *          AND lookup.hashing_key_id = ?
 *          AND lookup.hashing_version = ?
 *      )
 *      OR
 *      (
 *          lookup.email_index = ?
 *          AND lookup.hashing_key_id = ?
 *          AND lookup.hashing_version = ?
 *      )
 *      ...
 */

/*
 * UPDATE
 *   users
 * Set
 *   users.public_id = ?
 *   users.username = ?
 *   users.email = ?
 * WHERE
 *   users.lookup_id = ?
 *   AND
 *   (
 *       users.encryption_key_id <> ?
 *       OR
 *       users.encryption_version <> ?
 *   );
 **/