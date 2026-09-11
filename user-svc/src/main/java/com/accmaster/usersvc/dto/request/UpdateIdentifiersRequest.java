package com.accmaster.usersvc.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public record UpdateIdentifiersRequest(
    @NotBlank(message = "id is required") String id,
    String usernameType,
    @Pattern(regexp = "^07[01245678][0-9]{7}$", message = "Invalid mobile number") String mobile,
    @Pattern(regexp = "^(([5-9][0-9][01235678][0-9]{6}[vVxX])|([12][0-9]{3}[01235678][0-9]{7}))$", message = "Invalid NIC") String nic
) {}
