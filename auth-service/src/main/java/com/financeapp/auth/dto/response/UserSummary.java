package com.financeapp.auth.dto.response;

import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.domain.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {
    private String id;
    private String fullName;
    private String phone;
    private String email;
    private UserRole role;
    private UserStatus status;
    private Instant registeredAt;
}
