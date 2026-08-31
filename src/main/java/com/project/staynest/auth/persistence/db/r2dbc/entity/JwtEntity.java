package com.project.staynest.auth.persistence.db.r2dbc.entity;

import com.project.staynest.auth.validation.Validation;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("jwt")
public class JwtEntity {

    @Transient
    private final String CLASS_NAME = this.getClass().getSimpleName();

    @Id
    @Column("id")
    private Long id;

    @Column("subject")
    private String subject;

    @Column("refresh_jwt_id")
    private String refreshJwtId;

    @Column("access_jwt_id")
    private String accessJwtId;

    @Column("session_id")
    private String sessionId;

    @Column("refresh_expires_at")
    private Instant refreshExpiresAt;

    @Column("status")
    private String status;

    @Column("role")
    private String role;

    @Column("device_id")
    private String deviceId;

    @Column("user_agent")
    private String userAgent;


    public JwtEntity(){}

    public void setSubject(String subject){
        Validation.validate(subject, "subject", CLASS_NAME);

        this.subject = subject;
    }

    public void setRefreshJwtId(String refreshJwtId){
        Validation.validate(refreshJwtId, "refreshJwtId", CLASS_NAME);

        this.refreshJwtId = refreshJwtId;
    }

    public void setAccessJwtId(String accessJwtId){
        Validation.validate(accessJwtId, "accessJwtId", CLASS_NAME);

        this.accessJwtId = accessJwtId;
    }

    public void setSessionId(String sessionId){
        Validation.validate(sessionId, "sessionId", CLASS_NAME);

        this.sessionId = sessionId;
    }

    public void setRefreshExpiresAt(Instant refreshExpiresAt){
        Validation.validate(refreshExpiresAt, "refreshExpiresAt", CLASS_NAME);

        this.refreshExpiresAt = refreshExpiresAt;
    }

    public void setStatus(String status){
        Validation.validate(status, "status", CLASS_NAME);

        this.status = status;
    }

    public void setRole(String role){
        Validation.validate(role, "role", CLASS_NAME);

        this.role = role;
    }

    public void setDeviceId(String deviceId){
        Validation.validate(deviceId, "deviceId", CLASS_NAME);

        this.deviceId = deviceId;
    }

    public void setUserAgent(String userAgent){
        Validation.validate(userAgent, "userAgent", CLASS_NAME);

        this.userAgent = userAgent;
    }


    public String getSubject(){
        return this.subject;
    }

    public String getRefreshJwtId(){
        return this.refreshJwtId;
    }

    public String getAccessJwtId(){
        return this.accessJwtId;
    }

    public String getSessionId(){
        return this.sessionId;
    }

    public Instant getRefreshExpiresAt(){
        return this.refreshExpiresAt;
    }

    public String getStatus(){

        return this.status;
    }

    public String getRole(){
        return this.role;
    }

    public String getDeviceId(){
        return this.deviceId;
    }

    public String getUserAgent(){

        return this.userAgent;
    }

}
