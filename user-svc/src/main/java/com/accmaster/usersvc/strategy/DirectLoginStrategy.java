package com.accmaster.usersvc.strategy;

import com.accmaster.usersvc.domain.entity.UserEntity;
import com.accmaster.usersvc.domain.enums.UserType;
import com.accmaster.usersvc.dto.response.AuthResponse;
import com.accmaster.usersvc.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Direct (no OTP) login strategy — immediately issues a JWT access token.
 *
 * Currently not mapped to any user type (both ADMIN and STUDENT use OtpLoginStrategy).
 * Switch mapping in LoginStrategyFactory to re-enable for any user type.
 *
 * Replaces PHP DirectLoginStrategy (which set $_SESSION['user'] directly).
 * Here we issue a stateless JWT instead.
 */
@Component
@RequiredArgsConstructor
public class DirectLoginStrategy implements LoginStrategy {

    private final JwtUtil jwtUtil;

    @Override
    public boolean supports(UserType userType) {
        // Not currently mapped — can be enabled per user type via LoginStrategyFactory
        return false;
    }

    @Override
    public LoginResult initiate(UserEntity user) {
        String role = resolveRole(user);
        String token = jwtUtil.generateAccessToken(user.getId(), role, user.getUserType());

        AuthResponse authResponse = new AuthResponse(
                token,
                user.getId(),
                role,
                user.getUserType().name(),
                user.getUserType() == UserType.STUDENT
                        ? (user.getStudent() != null ? user.getStudent().getFname() : null)
                        : (user.getAdmin() != null ? user.getAdmin().getFname() : null),
                user.getUserType() == UserType.STUDENT
                        ? (user.getStudent() != null ? user.getStudent().getLname() : null)
                        : (user.getAdmin() != null ? user.getAdmin().getLname() : null),
                user.getMobile(),
                user.getEmail(),
                user.getNic(),
                user.getStudent() != null ? user.getStudent().getAcademicId() : null,
                user.getStudent() != null ? user.getStudent().getGender().name() : null,
                user.getProfile() != null ? user.getProfile().getUrl() : null,
                user.isForcePasswordReset(),
                user.getRegisteredAt()
        );

        return new DirectResult(authResponse);
    }

    private String resolveRole(UserEntity user) {
        if (user.getUserType() == UserType.ADMIN && user.getAdmin() != null) {
            return user.getAdmin().getRole().name();
        }
        return user.getUserType().name(); // "STUDENT"
    }
}
