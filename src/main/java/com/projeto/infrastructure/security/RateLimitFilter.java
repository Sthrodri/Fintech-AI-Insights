package com.projeto.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int REQUESTS_PER_MINUTE = 100;
    private static final String RATE_LIMIT_EXCEEDED_JSON = "{\"error\": \"Too Many Requests\", \"tenant\": \"%s\"}";

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final TenantContextHolder tenantContextHolder;

    public RateLimitFilter(TenantContextHolder tenantContextHolder) {
        this.tenantContextHolder = tenantContextHolder;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String tenantId = tenantContextHolder.getTenantId();

        if (tenantId == null || tenantId.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(tenantId, key -> createNewBucket());

        if (bucket.tryConsume(1)) {
            log.debug("Rate limit OK for tenant: {} (remaining: {})", tenantId, bucket.getAvailableTokens());
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for tenant: {}", tenantId);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(String.format(RATE_LIMIT_EXCEEDED_JSON, tenantId));
        }
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REQUESTS_PER_MINUTE)
                .refillGreedy(REQUESTS_PER_MINUTE, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
