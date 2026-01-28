package com.northstar.studentcard.infra.idempotency;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Idempotency guard for event processing.
 * Ensures a given message/event id is processed only once even if it is delivered multiple times.
 *
 * Implementation:
 * - Redis SETNX (set if absent) with TTL.
 * - If key already exists -> treat as duplicate and safely skip.
 */
@Service
public class RedisIdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public RedisIdempotencyService(StringRedisTemplate redisTemplate,
                                   @Value("${studentcard.idempotency.ttlSeconds}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    public boolean tryMarkProcessed(String idempotencyKey) {
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", ttl);
        return Boolean.TRUE.equals(success);
    }
}
