package com.freelms.search.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Feign client for Auth/User Service - used for reindexing users.
 */
@FeignClient(name = "auth-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/search-index")
    List<Map<String, Object>> getUsersForIndexing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size);

    @GetMapping("/api/v1/users/search-index/count")
    Long getUsersCount();
}
