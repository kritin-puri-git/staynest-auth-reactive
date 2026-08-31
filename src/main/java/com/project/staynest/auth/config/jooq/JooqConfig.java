package com.project.staynest.auth.config.jooq;

import io.r2dbc.spi.ConnectionFactory;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JooqConfig {

    @Bean
    public org.jooq.Configuration jooqConfiguration(
      @Qualifier("routingMysqlConnectionFactory")
      ConnectionFactory routingConnectionFactory
    ){
        DefaultConfiguration configuration = new DefaultConfiguration();

        configuration.setConnectionFactory(routingConnectionFactory);
        configuration.set(SQLDialect.MYSQL);

        return configuration;
    }

    @Bean
    public DSLContext dslContext(
            org.jooq.Configuration configuration
    ){
        return new DefaultDSLContext(configuration);
    }

}
