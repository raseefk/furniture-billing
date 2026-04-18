package com.furnitureshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "invoice_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Price snapshot at time of transaction - crucial for historical accuracy
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal soldAtPrice;

    @Column(nullable = false)
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal gstAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    // HSN snapshot for historical integrity
    private String hsnCode;
    private String productName; // Snapshot in case product is renamed

    public void calculateAmounts() {
        BigDecimal lineTotal = this.soldAtPrice.multiply(BigDecimal.valueOf(this.quantity));
        this.total = lineTotal.setScale(2, RoundingMode.HALF_UP);
        // GST per item (18% total: 9% CGST + 9% SGST)
        this.gstAmount = lineTotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getCgstAmount() {
        if (gstAmount == null) return BigDecimal.ZERO;
        return gstAmount.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getSgstAmount() {
        return getCgstAmount();
    }
}
