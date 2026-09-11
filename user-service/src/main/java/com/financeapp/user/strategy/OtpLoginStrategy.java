package com.financeapp.user.strategy;

import com.financeapp.user.domain.entity.UserEntity;
import com.financeapp.user.domain.enums.UserType;
import com.financeapp.user.dto.response.OtpChallengeResponse;
import com.financeapp.user.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * OTP 2FA login strategy.
 *
 * Step 1 (this class): Validate credentials (done in AuthService), generate OTP,
 *   store in Redis with 300s TTL, return txnId to client.
 * Step 2: Client presents txnId + OTP to /api/auth/login/verify (handled in AuthService.verifyOtpForLogin)
 *
 * Replaces PHP OtpLoginStrategy which used $_SESSION['temp_auth'] + updateOtp() DB write.
 * Here we use Redis exclusively — no DB write for OTP storage.
 */
@Component
@RequiredArgsConstructor
public class OtpLoginStrategy implements LoginStrategy {

    private static final String PURPOSE = "LOGIN_2FA";

    private final OtpService otpService;

    @Override
    public boolean supports(UserType userType) {
        // Both ADMIN and STUDENT use OTP strategy (matches PHP LoginStrategyFactory)
        return userType == UserType.ADMIN || userType == UserType.STUDENT;
    }

    @Override
    public LoginResult initiate(UserEntity user) {
        String otp   = otpService.generateOtp();
        String txnId = otpService.storeOtp(PURPOSE, user.getId(), user.getUsername(), otp);

        // In dev mode, OTP is logged to console.
        // In production: inject NotificationService here and send SMS asynchronously.

        return new OtpResult(new OtpChallengeResponse(txnId, "OTP sent to your registered mobile number."));
    }

    public static String getPurpose() {
        return PURPOSE;
    }
}
