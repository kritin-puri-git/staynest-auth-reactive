package com.project.staynest.auth.business.service.impl;

import com.project.staynest.auth.business.service.RateLimiterService;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private final ProxyManager<String> proxyManager;
    public RateLimiterServiceImpl(
            ProxyManager<String> proxyManager
    ){
        this.proxyManager = proxyManager;
    }

    @Override
    public Mono<Boolean> tryConsume(String key, BucketConfiguration configuration) {
        AsyncBucketProxy bucket = proxyManager
                .asAsync()
                .builder()
                .build(key,()-> CompletableFuture.completedFuture(configuration));
        return Mono.fromFuture(bucket.tryConsume(1));
    }
}
