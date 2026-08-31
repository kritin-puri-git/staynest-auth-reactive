package com.project.staynest.auth.config.mysql;

import com.project.staynest.auth.validation.Validation;

public class MysqlProperties {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private int maxPoolSize;

    private final String CLASS_NAME = this.getClass().getSimpleName();

    public void setHost(String host){
        try {
            Validation.validate(host,"host", CLASS_NAME);
        }catch(IllegalArgumentException exception){
            throw new IllegalStateException("ProxySQL host is not configured [" + CLASS_NAME + "]", exception);
        }

        this.host = host;
    }

    public void setPort(Integer port){
        try {
            Validation.validate(port,"port", CLASS_NAME);
        }catch(IllegalArgumentException exception){
            throw new IllegalStateException("ProxySQL port is not configured [" + CLASS_NAME + "]", exception);
        }

        this.port = port;
    }

    public void setDatabase(String database){
        try {
            Validation.validate(database,"database", CLASS_NAME);
        }catch(IllegalArgumentException exception){
            throw new IllegalStateException("Mysql database is not configured [" + CLASS_NAME + "]", exception);
        }

        this.database = database;
    }

    public void setUsername(String username){
        try {
            Validation.validate(username,"username", CLASS_NAME);
        }catch(IllegalArgumentException exception){
            throw new IllegalStateException("Mysql username is not configured [" + CLASS_NAME + "]", exception);
        }

        this.username = username;
    }

    public void setPassword(String password){
        try {
            Validation.validate(password,"password", CLASS_NAME);
        }catch(IllegalArgumentException exception){
            throw new IllegalStateException("Mysql password is not configured [" + CLASS_NAME + "]", exception);
        }

        this.password = password;
    }

    public void setMaximumPoolSize(Integer maxPoolSize){
        try {
            Validation.validate(maxPoolSize,"maxPoolSize", CLASS_NAME);
        }catch(IllegalArgumentException exception){
            throw new IllegalStateException("Mysql maximumPoolSize is not configured [" + CLASS_NAME + "]", exception);
        }

        this.maxPoolSize = maxPoolSize;
    }

    public String getHost(){
        return this.host;
    }
    public int getPort(){
        return this.port;
    }
    public String getDatabase(){
        return this.database;
    }
    public String getUsername(){
        return this.username;
    }
    public String getPassword(){
        return this.password;
    }
    public int getMaximumPoolSize(){
        return this.maxPoolSize;
    }
}
