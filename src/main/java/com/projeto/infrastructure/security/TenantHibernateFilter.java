package com.projeto.infrastructure.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class TenantHibernateFilter extends OncePerRequestFilter {

    public static final String TENANT_HIBERNATE_FILTER_NAME = "tenantFilter";

    private final TenantContextHolder tenantContextHolder;

    @PersistenceContext
    private EntityManager entityManager;

    public TenantHibernateFilter(TenantContextHolder tenantContextHolder) {
        this.tenantContextHolder = tenantContextHolder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = null;
        try {
            String tenantId = tenantContextHolder.getTenantId();
            if (tenantId != null && !tenantId.isBlank()) {
                filter = session.enableFilter(TENANT_HIBERNATE_FILTER_NAME);
                filter.setParameter("tenantId", UUID.fromString(tenantId));
            }
            filterChain.doFilter(request, response);
        } finally {
            if (filter != null) {
                session.disableFilter(TENANT_HIBERNATE_FILTER_NAME);
            }
        }
    }
}
