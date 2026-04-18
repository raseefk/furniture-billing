package com.furnitureshop.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.QUOTATION;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal cgstAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal sgstAmount = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    private String notes;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime convertedAt;

    private String createdBy;

    // GST rate constants
    public static final BigDecimal CGST_RATE = new BigDecimal("0.09");
    public static final BigDecimal SGST_RATE = new BigDecimal("0.09");

    public void calculateTotals() {
        BigDecimal sub = BigDecimal.ZERO;
        for (InvoiceItem item : items) {
            sub = sub.add(item.getTotal());
        }
        this.subtotal = sub.setScale(2, RoundingMode.HALF_UP);
        this.cgstAmount = sub.multiply(CGST_RATE).setScale(2, RoundingMode.HALF_UP);
        this.sgstAmount = sub.multiply(SGST_RATE).setScale(2, RoundingMode.HALF_UP);
        this.grandTotal = subtotal.add(cgstAmount).add(sgstAmount).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isQuotation() {
        return this.status == InvoiceStatus.QUOTATION;
    }

    public boolean isFinalBill() {
        return this.status == InvoiceStatus.FINAL_BILL;
    }

    public boolean isCancelled() {
        return this.status == InvoiceStatus.CANCELLED;
    }

    public String getDocumentTitle() {
        return this.status == InvoiceStatus.QUOTATION ? "Quotation" : "Tax Invoice";
    }
}
