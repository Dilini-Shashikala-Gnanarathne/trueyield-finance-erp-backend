package com.financeapp.user.strategy;

import com.financeapp.user.domain.entity.UserEntity;
import com.financeapp.user.domain.enums.UserType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring-idiomatic Strategy Factory.
 *
 * Spring automatically injects all LoginStrategy implementations into the List<LoginStrategy>.
 * Strategy resolution is done via LoginStrategy.supports(UserType) — zero hardcoded conditionals.
 *
 * Replaces PHP LoginStrategyFactory which used 'match' and 'new OtpLoginStrategy(...)'.
 * Here, Spring manages instantiation and lifecycle of all strategies.
 */
@Component
@RequiredArgsConstructor
public class LoginStrategyFactory {

    /** Spring injects all @Component implementations automatically. */
    private final List<LoginStrategy> strategies;

    public LoginStrategy.LoginResult execute(UserEntity user) {
        UserType userType = user.getUserType();
        LoginStrategy strategy = strategies.stream()
                .filter(s -> s.supports(userType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No login strategy defined for user type: " + userType
                ));
        return strategy.initiate(user);
    }
}
