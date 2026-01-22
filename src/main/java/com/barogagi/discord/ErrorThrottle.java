package com.barogagi.discord;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ErrorThrottle {

    private final Map<String, Long> cache = new ConcurrentHashMap<>();
    private static final long INTERVAL = 60_000;

    public boolean shouldNotify(String key) {
        long now = System.currentTimeMillis();
        return cache.compute(key, (k, last) ->
                (last == null || now - last > INTERVAL) ? now : last
        ) == now;
    }
}

