package com.financeapp.user.config;

import com.financeapp.user.security.SecurityPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Configures JPA Auditing — provides the current userId as the auditor.
 * Automatically populates @CreatedBy and @LastModifiedBy on AuditableEntity subclasses.
 */
@Configuration
public class AuditConfig {

    @Bean(name = "auditorProvider")
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("SYSTEM");
            }
            if (auth.getPrincipal() instanceof SecurityPrincipal principal) {
                return Optional.of(principal.getUserId());
            }
            return Optional.of("SYSTEM");
        };
    }
}
