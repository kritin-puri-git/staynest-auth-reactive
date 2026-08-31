package com.project.staynest.auth.config.mysql;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mysql.master")
public class MysqlMasterProperties extends MysqlProperties{


}
