package com.techmath.ecommerce.infrastructure.config;

import com.techmath.ecommerce.domain.entities.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<User> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (isNotAuthenticated(auth)) {
                throw new IllegalStateException("User not authenticated");
            }

            User user = (User) auth.getPrincipal();
            return Optional.ofNullable(user);
        };
    }

    private boolean isNotAuthenticated(Authentication auth) {
        return Objects.isNull(auth) || !auth.isAuthenticated() || auth.getClass() == AnonymousAuthenticationToken.class;
    }

}
