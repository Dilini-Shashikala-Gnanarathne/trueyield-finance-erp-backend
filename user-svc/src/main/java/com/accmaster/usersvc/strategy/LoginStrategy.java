package com.accmaster.usersvc.strategy;

import com.accmaster.usersvc.domain.entity.UserEntity;
import com.accmaster.usersvc.domain.enums.UserType;
import com.accmaster.usersvc.dto.response.AuthResponse;
import com.accmaster.usersvc.dto.response.OtpChallengeResponse;

/**
 * Login strategy contract.
 *
 * Each implementation handles a different authentication flow:
 *   - DirectLoginStrategy: verifies credentials and immediately returns a JWT
 *   - OtpLoginStrategy: issues OTP challenge, returns txnId
 *
 * Replaces PHP LoginStrategyInterface.
 * Spring IoC manages instantiation — no `new` keyword anywhere.
 */
public interface LoginStrategy {

    /** Returns true if this strategy handles the given user type. */
    boolean supports(UserType userType);

    /** Sealed return type — either an AuthResponse (direct) or OtpChallengeResponse (OTP). */
    sealed interface LoginResult permits LoginStrategy.DirectResult, LoginStrategy.OtpResult {}

    record DirectResult(AuthResponse authResponse) implements LoginResult {}
    record OtpResult(OtpChallengeResponse challenge) implements LoginResult {}

    /** Initiates authentication for the given user. */
    LoginResult initiate(UserEntity user);
}
