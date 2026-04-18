package com.furnitureshop.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItemDto {
    private Long productId;
    private Integer quantity;
    private BigDecimal soldAtPrice; // can override price
}
