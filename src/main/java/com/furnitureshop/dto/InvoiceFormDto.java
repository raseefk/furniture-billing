package com.furnitureshop.dto;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceFormDto {
    private Long customerId;
    private String notes;
    private String status; // QUOTATION or FINAL_BILL

    @Builder.Default
    private List<InvoiceItemDto> items = new ArrayList<>();
}
