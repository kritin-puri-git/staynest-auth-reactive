package com.project.staynest.auth.config.mysql.r2dbc;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

@Configuration
@EnableR2dbcRepositories(
        basePackages = "com.project.staynest.auth"
)
@EnableR2dbcAuditing
public class R2dbcConfig {

    @Primary
    @Bean
    public ReactiveTransactionManager reactiveTransactionManager(
            @Qualifier("routingMysqlConnectionFactory")
            ConnectionFactory routingMysqlConnectionFactory
    ){
        return new R2dbcTransactionManager(routingMysqlConnectionFactory);
    }
}
