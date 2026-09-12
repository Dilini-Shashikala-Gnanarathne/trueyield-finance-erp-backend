package com.financeapp.auth;

import com.financeapp.auth.config.SecurityConfig;
import com.financeapp.auth.controller.AuthController;
import com.financeapp.auth.domain.enums.UserRole;
import com.financeapp.auth.exception.GlobalExceptionHandler;
import com.financeapp.auth.security.JwtAuthenticationFilter;
import com.financeapp.auth.security.SecurityPrincipal;
import com.financeapp.auth.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
class AuthControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        // Ensure mock filter passes through the filter chain
        doAnswer(invocation -> {
            HttpServletRequest request = invocation.getArgument(0);
            HttpServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("AUTH-004: FARMER role can access farmer endpoint")
    void shouldAllowFarmerToAccessFarmerEndpoint() throws Exception {
        SecurityPrincipal farmerPrincipal = new SecurityPrincipal(
                "f1", "Farmer Sunil", "0771234567", "sunil@example.com", UserRole.FARMER
        );

        mockMvc.perform(get("/api/v1/auth/test/farmer")
                        .with(user(farmerPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("FARMER"));
    }

    @Test
    @DisplayName("AUTH-004: BUYER role is forbidden from accessing farmer endpoint")
    void shouldForbidBuyerFromAccessingFarmerEndpoint() throws Exception {
        SecurityPrincipal buyerPrincipal = new SecurityPrincipal(
                "b1", "Buyer Kamal", "0719876543", "kamal@example.com", UserRole.BUYER
        );

        mockMvc.perform(get("/api/v1/auth/test/farmer")
                        .with(user(buyerPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AUTH-004: BUYER role can access buyer endpoint")
    void shouldAllowBuyerToAccessBuyerEndpoint() throws Exception {
        SecurityPrincipal buyerPrincipal = new SecurityPrincipal(
                "b1", "Buyer Kamal", "0719876543", "kamal@example.com", UserRole.BUYER
        );

        mockMvc.perform(get("/api/v1/auth/test/buyer")
                        .with(user(buyerPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("BUYER"));
    }

    @Test
    @DisplayName("AUTH-004: FARMER role is forbidden from accessing buyer endpoint")
    void shouldForbidFarmerFromAccessingBuyerEndpoint() throws Exception {
        SecurityPrincipal farmerPrincipal = new SecurityPrincipal(
                "f1", "Farmer Sunil", "0771234567", "sunil@example.com", UserRole.FARMER
        );

        mockMvc.perform(get("/api/v1/auth/test/buyer")
                        .with(user(farmerPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AUTH-004: ADMIN role can access admin endpoint")
    void shouldAllowAdminToAccessAdminEndpoint() throws Exception {
        SecurityPrincipal adminPrincipal = new SecurityPrincipal(
                "a1", "Admin User", "0770000000", "admin@trueyield.com", UserRole.ADMIN
        );

        mockMvc.perform(get("/api/v1/auth/test/admin")
                        .with(user(adminPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("AUTH-004: Non-admin is forbidden from accessing admin endpoint")
    void shouldForbidNonAdminFromAccessingAdminEndpoint() throws Exception {
        SecurityPrincipal farmerPrincipal = new SecurityPrincipal(
                "f1", "Farmer Sunil", "0771234567", "sunil@example.com", UserRole.FARMER
        );

        mockMvc.perform(get("/api/v1/auth/test/admin")
                        .with(user(farmerPrincipal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("AUTH-004: Unauthenticated request is rejected")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/v1/auth/test/farmer"))
                .andExpect(status().isForbidden());
    }
}
