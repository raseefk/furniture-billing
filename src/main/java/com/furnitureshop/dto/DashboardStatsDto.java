package com.furnitureshop.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalProducts;
    private long outOfStockCount;
    private long lowStockCount;
    private long totalCustomers;
    private long totalQuotations;
    private long totalFinalBills;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
}
