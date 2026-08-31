package com.project.staynest.auth.config.mysql;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mysql.replica")
public class MysqlReplicaProperties extends MysqlProperties {

}
