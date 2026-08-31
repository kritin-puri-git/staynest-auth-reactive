package com.project.staynest.auth.config.mysql.r2dbc;


import com.project.staynest.auth.config.mysql.MysqlMasterProperties;
import com.project.staynest.auth.config.mysql.MysqlProperties;
import com.project.staynest.auth.config.mysql.MysqlReplicaProperties;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

import io.r2dbc.spi.Option;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MysqlConnectionFactoryConfig {

    @Bean(name = "masterMysqlConnectionFactory")
    public ConnectionFactory masterMysqlConnectionFactory(
            MysqlMasterProperties masterMysqlProperties
    ){
        return createMysqlConnectionFactory(masterMysqlProperties);
    }

    @Bean(name = "replicaMysqlConnectionFactory")
    public ConnectionFactory replicaMysqlConnectionFactory(
            MysqlReplicaProperties replicaMysqlProperties
    ){
        return createMysqlConnectionFactory(replicaMysqlProperties);
    }

    private ConnectionFactory createMysqlConnectionFactory(
            MysqlProperties properties
    ){
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(DRIVER, "mysql")
                .option(HOST, properties.getHost())
                .option(PORT, properties.getPort())
                .option(DATABASE, properties.getDatabase())
                .option(USER, properties.getUsername())
                .option(PASSWORD, properties.getPassword())
                .option(
                        Option.valueOf("serverZoneId"),
                        ZoneId.of("UTC")
                )
                .build();

        return ConnectionFactories.get(options);
    }

    @Primary
    @Bean(name = "routingMysqlConnectionFactory")
    public ConnectionFactory routingMysqlConnectionFactory(
            @Qualifier("masterMysqlConnectionFactory")
            ConnectionFactory masterMysqlConnectionFactory,
            @Qualifier("replicaMysqlConnectionFactory")
            ConnectionFactory replicaMysqlConnectionFactory
    ){

        RoutingMysqlConnectionFactory routingMysqlConnectionFactory = new RoutingMysqlConnectionFactory();

        Map<Object, ConnectionFactory> connectionFactories = new HashMap<>();

        connectionFactories.put(MysqlConnectionFactoryType.MASTER, masterMysqlConnectionFactory);
        connectionFactories.put(MysqlConnectionFactoryType.REPLICA, replicaMysqlConnectionFactory);

        routingMysqlConnectionFactory.setTargetConnectionFactories(connectionFactories);

        routingMysqlConnectionFactory.setDefaultTargetConnectionFactory(masterMysqlConnectionFactory);

        routingMysqlConnectionFactory.initialize();

        return routingMysqlConnectionFactory;

    }

}
