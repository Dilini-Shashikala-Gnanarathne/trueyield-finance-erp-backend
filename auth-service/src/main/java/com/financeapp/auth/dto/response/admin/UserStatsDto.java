package com.financeapp.auth.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsDto {
    private long totalUsers;
    private long totalFarmers;
    private long totalBuyers;
    private long totalAdmins;
}
