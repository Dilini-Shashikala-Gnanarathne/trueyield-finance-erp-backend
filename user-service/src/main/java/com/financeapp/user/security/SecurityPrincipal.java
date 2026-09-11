package com.financeapp.user.security;

import com.financeapp.user.domain.enums.AdminRole;
import com.financeapp.user.domain.enums.UserType;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security principal carrying the current authenticated user's identity.
 * Populated by JwtAuthenticationFilter from validated JWT claims.
 */
@Getter
public class SecurityPrincipal implements UserDetails {

    private final String userId;
    private final String username;
    private final String role;      // e.g., "SUPER_ADMIN", "SUB_ADMIN", "STUDENT"
    private final UserType userType;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityPrincipal(String userId, String username, String role, UserType userType) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.userType = userType;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword()  { return null; }
    @Override public String getUsername()  { return username; }
    @Override public boolean isAccountNonExpired()   { return true; }
    @Override public boolean isAccountNonLocked()    { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
