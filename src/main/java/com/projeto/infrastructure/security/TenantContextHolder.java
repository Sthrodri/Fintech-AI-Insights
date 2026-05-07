package com.projeto.infrastructure.security;

import org.springframework.stereotype.Component;

@Component
public class TenantContextHolder {

    private final ThreadLocal<String> tenantIdHolder = new ThreadLocal<>();

    public void setTenantId(String tenantId) {
        tenantIdHolder.set(tenantId);
    }

    public String getTenantId() {
        return tenantIdHolder.get();
    }

    public void clear() {
        tenantIdHolder.remove();
    }
}
