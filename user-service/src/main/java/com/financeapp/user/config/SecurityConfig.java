package com.financeapp.user.config;

import com.financeapp.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless Spring Security configuration.
 *
 * Key decisions:
 *  - STATELESS session (no HttpSession — replaces PHP $_SESSION)
 *  - JWT filter injected before UsernamePasswordAuthenticationFilter
 *  - @EnableMethodSecurity enables @PreAuthorize at method level (replaces PHP authorizeRole())
 *  - BCrypt password encoder (replaces PHP password_hash(PASSWORD_DEFAULT))
 *  - CSRF disabled (safe for stateless API)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints (no token required)
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/register/verify",
                                "/api/auth/login",
                                "/api/auth/login/verify",
                                "/api/auth/logout",
                                "/api/auth/password/initiate",
                                "/api/auth/password/complete"
                        ).permitAll()
                        // Swagger / OpenAPI docs
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**"
                        ).permitAll()
                        // Actuator health endpoint
                        .requestMatchers("/actuator/health").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * BCryptPasswordEncoder with strength 12 (matches PHP PASSWORD_DEFAULT which uses Bcrypt).
     * Strength 12 is production-grade and takes ~250ms — slows brute force attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
