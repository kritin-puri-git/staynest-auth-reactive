package com.project.staynest.auth.persistence.db.r2dbc.entity;

import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Arrays;

@Table("users_lookup")
public class UsersLookupEntity {




    @Id
    @Column("id")
    private Long id;

    @Column("public_id_index")
    private byte[] publicIdIndex;

    @Column("username_index")
    private byte[] usernameIndex;

    @Column("email_index")
    private byte[] emailIndex;

    @Column("hashing_version")
    private Short hashingVersion;

    @Column("hashing_key_id")
    private Short hashingKeyId;

    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;

    protected UsersLookupEntity(){}

    public void setPublicIdIndex(byte[] publicIdIndex) {
        if(publicIdIndex == null){
            throw new IllegalArgumentException(
                    "Public Id Index cannot be null in UserLookupEntity"
            );
        }
        this.publicIdIndex = Arrays.copyOf(
                publicIdIndex,
                publicIdIndex.length
        );
    }

    public void setUsernameIndex(byte[] usernameIndex) {
        if(usernameIndex == null){
            throw new IllegalArgumentException(
                    "Username Index cannot be null in UserLookupEntity"
            );
        }
        this.usernameIndex = Arrays.copyOf(
                usernameIndex,
                usernameIndex.length
        );
    }

    public void setEmailIndex(byte[] emailIndex){
        if(emailIndex == null){
            throw new IllegalArgumentException(
                    "Email Index cannot be null in UserLookupEntity"
            );
        }
        this.emailIndex = Arrays.copyOf(
                emailIndex,
                emailIndex.length
        );
    }

    public void setHashingVersion(short hashingVersion){
        if(hashingVersion <= 0){
            throw new IllegalArgumentException(
                    "Hashing Version must be positive in UsersLookupEntity"
            );
        }
        this.hashingVersion = hashingVersion;
    }

    public void setHashingKeyId(short hashingKeyId){
        if(hashingKeyId <= 0){
            throw new IllegalArgumentException(
                    "Hashing key id must be positive in UsersLookupEntity"
            );
        }
        this.hashingKeyId = hashingKeyId;
    }

    public Long getId(){
        return this.id;
    }

    public byte[] getPublicIdIndex(){

        return this.publicIdIndex == null ? null : Arrays.copyOf(
                this.publicIdIndex,
                this.publicIdIndex.length
        );

    }

    public byte[] getUsernameIndex(){
        return this.usernameIndex == null ? null : Arrays.copyOf(
                this.usernameIndex,
                this.usernameIndex.length
        );
    }

    public byte[] getEmailIndex(){
        return this.emailIndex == null ? null : Arrays.copyOf(
                this.emailIndex,
                this.emailIndex.length
        );
    }

    public Short getHashingVersion(){
        return this.hashingVersion;
    }

    public Short getHashingKeyId(){
        return this.hashingKeyId;
    }

    public Instant getCreatedAt(){
        return this.createdAt;
    }

    public Instant getUpdatedAt(){
        return this.updatedAt;
    }

    public static class Builder{
        private byte[] publicIdIndex;
        private byte[] usernameIndex;
        private byte[] emailIndex;
        private short hashingVersion;
        private short hashingKeyId;


        public Builder publicIdIndex(byte[] publicIdIndex){
            if(publicIdIndex == null){
                throw new IllegalArgumentException(
                        "Public Id Index Id cannot be null in UsersLookupEntity Builder"
                );
            }
            this.publicIdIndex = Arrays.copyOf(
                    publicIdIndex,
                    publicIdIndex.length
            );
            return this;
        }

        public Builder usernameIndex(byte[] usernameIndex){
            if(usernameIndex == null){
                throw new IllegalArgumentException(
                        "Username Index cannot be null in UsersLookupEntity Builder"
                );
            }
            this.usernameIndex = Arrays.copyOf(
                    usernameIndex,
                    usernameIndex.length
            );
            return this;
        }

        public Builder emailIndex(byte[] emailIndex){
            if(emailIndex == null){
                throw new IllegalArgumentException(
                        "Email Index cannot be null in UsersLookupEntity Builder"
                );
            }
            this.emailIndex = Arrays.copyOf(
                    emailIndex,
                    emailIndex.length
            );
            return this;
        }

        public Builder hashingVersion(short hashingVersion){
            if(hashingVersion <= 0){
                throw new IllegalArgumentException(
                        "Hashing version must be positive in UsersLookupEntity Builder"
                );
            }
            this.hashingVersion = hashingVersion;
            return this;
        }

        public Builder hashingKeyId(short hashingKeyId){
            if(hashingKeyId <= 0){
                throw new IllegalArgumentException(
                        "Hashing key id must be positive in UsersLookupEntity Builder"
                );
            }
            this.hashingKeyId = hashingKeyId;
            return this;
        }



        public UsersLookupEntity build(){
            if(this.publicIdIndex == null){
                throw new UnexpectedIllegalStateException(
                        "Public Id Index cannot be null in UsersLookupEntity Builder"
                );
            }
            if(this.usernameIndex == null){
                throw new UnexpectedIllegalStateException(
                        "Username Index cannot be null in UsersLookupEntity Builder"
                );
            }
            if(this.emailIndex == null){
                throw new UnexpectedIllegalStateException(
                        "Email Index cannot be null in UsersLookupEntity Builder"
                );
            }
            if(this.hashingKeyId <= 0){
                throw new UnexpectedIllegalStateException(
                        "Hashing key id must be positive in UsersLookupEntity Builder"
                );
            }
            if(this.hashingVersion <= 0){
                throw new UnexpectedIllegalStateException(
                        "Hashing version must be positive in UsersLookupEntity Builder"
                );
            }

            UsersLookupEntity usersLookupEntity = new UsersLookupEntity();

            usersLookupEntity.setPublicIdIndex(this.publicIdIndex);
            usersLookupEntity.setUsernameIndex(this.usernameIndex);
            usersLookupEntity.setEmailIndex(this.emailIndex);
            usersLookupEntity.setHashingKeyId(this.hashingKeyId);
            usersLookupEntity.setHashingVersion(this.hashingVersion);

            return usersLookupEntity;
        }
    }
    public static Builder builder(){
        return new Builder();
    }

}