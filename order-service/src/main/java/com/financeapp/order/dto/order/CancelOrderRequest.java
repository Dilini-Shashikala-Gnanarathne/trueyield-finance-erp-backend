package com.financeapp.order.dto.order;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CancelOrderRequest {

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
