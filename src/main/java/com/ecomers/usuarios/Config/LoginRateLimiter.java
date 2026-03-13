package com.ecomers.usuarios.Config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginRateLimiter {

    // Un bucket por IP — se limpia automáticamente cuando expira
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip) {
        return buckets.computeIfAbsent(ip, this::crearBucket);
    }

    private Bucket crearBucket(String ip) {
        // Máximo 5 intentos por minuto por IP
        Bandwidth limite = Bandwidth.classic(
                5,
                Refill.intervally(5, Duration.ofMinutes(1))
        );
        return Bucket.builder()
                .addLimit(limite)
                .build();
    }

    public boolean intentoPermitido(String ip) {
        return resolveBucket(ip).tryConsume(1);
    }
}
