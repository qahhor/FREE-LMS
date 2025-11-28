package com.freelms.search.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Fallback for User Service client.
 */
@Component
public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public List<Map<String, Object>> getUsersForIndexing(int page, int size) {
        log.warn("Auth service unavailable, returning empty list");
        return Collections.emptyList();
    }

    @Override
    public Long getUsersCount() {
        log.warn("Auth service unavailable, returning 0");
        return 0L;
    }
}
