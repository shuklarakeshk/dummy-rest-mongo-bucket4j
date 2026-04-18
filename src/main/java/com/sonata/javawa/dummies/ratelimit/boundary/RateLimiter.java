package com.sonata.javawa.dummies.ratelimit.boundary;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.mongodb_async.Bucket4jMongoDBAsync;
import io.github.bucket4j.mongodb_async.MongoDBAsyncCompareAndSwapBasedProxyManager;
import io.quarkus.mongodb.reactive.ReactiveMongoClient;
import io.quarkus.runtime.Startup;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

@ApplicationScoped
@RequiredArgsConstructor
@Startup
public class RateLimiter {
    private final ReactiveMongoClient reactiveMongoClient;
    private BucketConfiguration bucketConfiguration;
    private MongoDBAsyncCompareAndSwapBasedProxyManager<String> proxyManager;
    @PostConstruct
    public void init() {
        var database = reactiveMongoClient.unwrap().getDatabase("rate_limit_buckets");
        var collection = database.getCollection("rate_limit_buckets");
        var builder = Bucket4jMongoDBAsync.compareAndSwapBasedBuilder(collection)
                .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(ofSeconds(10)));

        proxyManager = builder.build();
        bucketConfiguration = BucketConfiguration.builder()
                .addLimit(limit -> limit.capacity(20).refillGreedy(20, ofMinutes(1)))
                .build();
    }

    public Uni<Boolean> isRateLimited(String key) {
        var attempt =  proxyManager.asAsync().getProxy(key, () -> CompletableFuture.supplyAsync(() -> bucketConfiguration)).tryConsume(1);
        return Uni.createFrom().completionStage(attempt).map(e -> !e);
    }


}
