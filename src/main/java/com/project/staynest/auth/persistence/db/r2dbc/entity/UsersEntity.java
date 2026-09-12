package com.project.staynest.auth.persistence.db.r2dbc.entity;

import com.project.staynest.auth.errorhandling.exceptions.unexpected.UnexpectedIllegalStateException;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Arrays;

@Table("users")
public class UsersEntity implements Persistable<Long> {

    @Id
    @Column("lookup_id")
    private Long userLookupId;

    @Column("public_id")
    private byte[] publicId;

    @Column("username")
    private byte[] username;

    @Column("email")
    private byte[] email;

    @Column("encryption_version")
    private Short encryptionVersion;

    @Column("encryption_key_id")
    private Short encryptionKeyId;

    @Column("status")
    private String status;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    protected UsersEntity(){}

    public void setUserLookupId(Long userLookupId) {

        if(userLookupId == null || userLookupId <= 0){
            throw new IllegalArgumentException(
                    "User Lookup Id must be positive in UsersEntity"
            );
        }
        this.userLookupId = userLookupId;
    }

    public void setPublicId(byte[] publicId) {
        if(publicId == null){
            throw new IllegalArgumentException(
                    "Public Id cannot be null in UsersEntity"
            );
        }
        this.publicId = Arrays.copyOf(
                publicId,
                publicId.length
        );
    }

    public void setUsername(byte[] username) {
        if(username == null){
            throw new IllegalArgumentException(
                    "Username cannot be null in UsersEntity"
            );
        }
        this.username = Arrays.copyOf(
                username,
                username.length
        );
    }

    public void setEmail(byte[] email){
        if(email == null){
            throw new IllegalArgumentException(
                    "Email cannot be null in UsersEntity"
            );
        }
        this.email = Arrays.copyOf(
                email,
                email.length
        );
    }

    public void setEncryptionVersion(short encryptionVersion){
        if(encryptionVersion <= 0){
            throw new IllegalArgumentException(
                    "Encryption version must be positive in UsersEntity"
            );
        }
        this.encryptionVersion = encryptionVersion;
    }

    public void setEncryptionKeyId(short encryptionKeyId){
        if(encryptionKeyId <= 0){
            throw new IllegalArgumentException(
                    "Encryption key id must be positive in UsersEntity"
            );
        }
        this.encryptionKeyId = encryptionKeyId;
    }

    public void setStatus(String status){
        if(status == null || status.isBlank()){
            throw new IllegalArgumentException(
                    "UserStatus cannot be null or blank in UsersEntity"
            );
        }
        this.status = status.trim();
    }

    public Long getUserLookupId(){

        return this.userLookupId;
    }

    public byte[] getPublicId(){

        return this.publicId == null ? null : Arrays.copyOf(
                this.publicId,
                this.publicId.length
        );

    }

    public byte[] getUsername(){
        return this.username == null ? null : Arrays.copyOf(
                this.username,
                this.username.length
        );
    }

    public byte[] getEmail(){
        return this.email == null ? null : Arrays.copyOf(
                this.email,
                this.email.length
        );
    }

    public Short getEncryptionVersion(){
        return this.encryptionVersion;
    }

    public Short getEncryptionKeyId(){
        return this.encryptionKeyId;
    }

    public String getStatus(){
        return this.status;
    }

    public Instant getCreatedAt(){
        return this.createdAt;
    }

    public Instant getUpdatedAt(){
        return this.updatedAt;
    }

    @Override
    public @Nullable Long getId() {
        return this.userLookupId;
    }

    @Transient
    private boolean isNew = false;

    public void setIsNew(boolean isNew){
        this.isNew = isNew;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public static class Builder{
        private Long userLookupId;
        private byte[] publicId;
        private byte[] username;
        private byte[] email;
        private short encryptionVersion;
        private short encryptionKeyId;
        private String status;

        public Builder userLookupId(Long userLookupId){
            if(userLookupId == null || userLookupId <= 0){
                throw new IllegalArgumentException(
                        "User Lookup Id must be positive in UsersEntity Builder"
                );
            }
            this.userLookupId = userLookupId;
            return this;
        }

        public Builder publicId(byte[] publicId){
            if(publicId == null){
                throw new IllegalArgumentException(
                        "Public Id cannot be null in UsersEntity Builder"
                );
            }
            this.publicId = Arrays.copyOf(
                    publicId,
                    publicId.length
            );
            return this;
        }

        public Builder username(byte[] username){
            if(username == null){
                throw new IllegalArgumentException(
                        "Username cannot be null in UsersEntity Builder"
                );
            }
            this.username = Arrays.copyOf(
                    username,
                    username.length
            );
            return this;
        }

        public Builder email(byte[] email){
            if(email == null){
                throw new IllegalArgumentException(
                        "Email cannot be null in UsersEntity Builder"
                );
            }
            this.email = Arrays.copyOf(
                    email,
                    email.length
            );
            return this;
        }

        public Builder encryptionVersion(short encryptionVersion){
            if(encryptionVersion <= 0){
                throw new IllegalArgumentException(
                        "Encryption version must be positive in UsersEntity Builder"
                );
            }
            this.encryptionVersion = encryptionVersion;
            return this;
        }

        public Builder encryptionKeyId(short encryptionKeyId){
            if(encryptionKeyId <= 0){
                throw new IllegalArgumentException(
                        "Encryption key id must be positive in UsersEntity Builder"
                );
            }
            this.encryptionKeyId = encryptionKeyId;
            return this;
        }

        public Builder status(String status){
            if(status == null || status.isBlank()){
                throw new IllegalArgumentException(
                        "UserStatus cannot be null or blank in UsersEntity Builder"
                );
            }
            this.status = status.trim();
            return this;
        }

        public UsersEntity build(){
            if(this.userLookupId == null){
                throw new UnexpectedIllegalStateException(
                        "User Lookup Id cannot be null in UsersEntity Builder"
                );
            }
            if(this.publicId == null){
                throw new UnexpectedIllegalStateException(
                        "Public Id cannot be null in UsersEntity Builder"
                );
            }
            if(this.username == null){
                throw new UnexpectedIllegalStateException(
                        "Username cannot be null in UsersEntity Builder"
                );
            }
            if(this.email == null){
                throw new UnexpectedIllegalStateException(
                        "Email cannot be null in UsersEntity Builder"
                );
            }
            if(this.encryptionKeyId <= 0){
                throw new UnexpectedIllegalStateException(
                        "Encryption key id must be positive in UsersEntity Builder"
                );
            }
            if(this.encryptionVersion <= 0){
                throw new UnexpectedIllegalStateException(
                        "Encryption version must be positive in UsersEntity Builder"
                );
            }
            if(this.status == null || this.status.isBlank()){
                throw new UnexpectedIllegalStateException(
                        "UserStatus cannot be null or blank in UsersEntity Builder"
                );
            }
            UsersEntity usersEntity = new UsersEntity();

            usersEntity.setUserLookupId(this.userLookupId);
            usersEntity.setPublicId(this.publicId);
            usersEntity.setUsername(this.username);
            usersEntity.setEmail(this.email);
            usersEntity.setEncryptionKeyId(this.encryptionKeyId);
            usersEntity.setEncryptionVersion(this.encryptionVersion);
            usersEntity.setStatus(this.status);

            return usersEntity;
        }
    }
    public static Builder builder(){
        return new Builder();
    }

}