package com.project.staynest.auth.config.mysql.r2dbc;
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;


public class RoutingMysqlConnectionFactory extends AbstractRoutingConnectionFactory {

    @Override
    protected Mono<Object> determineCurrentLookupKey() {
        return TransactionSynchronizationManager
                .forCurrentTransaction()
                .map(transactionSynchronizationManager ->
                        (Object)(transactionSynchronizationManager.isCurrentTransactionReadOnly()
                                ? MysqlConnectionFactoryType.REPLICA
                                : MysqlConnectionFactoryType.MASTER)
                )
                .onErrorResume(NoTransactionException.class,
                        e -> Mono.just((Object) MysqlConnectionFactoryType.MASTER)
                        )
                ;
    }
}
