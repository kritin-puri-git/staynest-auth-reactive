package com.project.staynest.auth.business.service;

import io.github.bucket4j.BucketConfiguration;
import reactor.core.publisher.Mono;

public interface RateLimiterService {
    Mono<Boolean> tryConsume(String key, BucketConfiguration configuration);
}
