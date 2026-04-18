package com.furnitureshop.service;

import com.furnitureshop.dto.InvoiceFormDto;
import com.furnitureshop.dto.InvoiceItemDto;
import com.furnitureshop.model.*;
import com.furnitureshop.repository.CustomerRepository;
import com.furnitureshop.repository.InvoiceRepository;
import com.furnitureshop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          CustomerRepository customerRepository,
                          ProductRepository productRepository,
                          ProductService productService) {
        this.invoiceRepository = invoiceRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    /**
     * Creates a Quotation WITHOUT deducting stock.
     */
    public Invoice createQuotation(InvoiceFormDto form, String createdBy) {
        return createInvoice(form, InvoiceStatus.QUOTATION, createdBy);
    }

    /**
     * Creates a Final Bill AND deducts stock immediately.
     */
    public Invoice createFinalBill(InvoiceFormDto form, String createdBy) {
        Invoice invoice = createInvoice(form, InvoiceStatus.FINAL_BILL, createdBy);
        deductStockForInvoice(invoice);
        return invoice;
    }

    /**
     * Converts an existing Quotation to a Final Bill.
     * Stock is deducted ONLY at this point.
     */
    public Invoice convertToFinalBill(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        if (!invoice.isQuotation()) {
            throw new IllegalStateException("Only quotations can be converted to final bills.");
        }

        // Validate stock availability before deducting
        for (InvoiceItem item : invoice.getItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for '" + product.getName() +
                        "'. Available: " + product.getStockQuantity() +
                        ", Required: " + item.getQuantity());
            }
        }

        // Deduct stock
        deductStockForInvoice(invoice);

        // Update status
        invoice.setStatus(InvoiceStatus.FINAL_BILL);
        invoice.setConvertedAt(LocalDateTime.now());

        // Reassign invoice number as a bill number
        String billNumber = generateInvoiceNumber("BILL");
        invoice.setInvoiceNumber(billNumber);

        return invoiceRepository.save(invoice);
    }

    /**
     * Cancel a quotation (does NOT affect stock).
     */
    public Invoice cancelQuotation(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        if (!invoice.isQuotation()) {
            throw new IllegalStateException("Only quotations can be cancelled.");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findById(Long id) {
        return invoiceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getQuotations() {
        return invoiceRepository.findByStatusOrderByCreatedAtDesc(InvoiceStatus.QUOTATION);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getFinalBills() {
        return invoiceRepository.findByStatusOrderByCreatedAtDesc(InvoiceStatus.FINAL_BILL);
    }

    @Transactional(readOnly = true)
    public List<Invoice> searchQuotations(String query) {
        if (query == null || query.isBlank()) return getQuotations();
        return invoiceRepository.searchByStatus(InvoiceStatus.QUOTATION, query.trim());
    }

    @Transactional(readOnly = true)
    public List<Invoice> searchFinalBills(String query) {
        if (query == null || query.isBlank()) return getFinalBills();
        return invoiceRepository.searchByStatus(InvoiceStatus.FINAL_BILL, query.trim());
    }

    @Transactional(readOnly = true)
    public List<Invoice> searchAll(String query) {
        if (query == null || query.isBlank())
            return invoiceRepository.findTop10ByOrderByCreatedAtDesc();
        return invoiceRepository.searchAll(query.trim());
    }

    @Transactional(readOnly = true)
    public List<Invoice> getRecentBills() {
        return invoiceRepository.findTop10ByStatusOrderByCreatedAtDesc(InvoiceStatus.FINAL_BILL);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getRecentQuotations() {
        return invoiceRepository.findTop10ByStatusOrderByCreatedAtDesc(InvoiceStatus.QUOTATION);
    }

    @Transactional(readOnly = true)
    public long countQuotations() {
        return invoiceRepository.countByStatus(InvoiceStatus.QUOTATION);
    }

    @Transactional(readOnly = true)
    public long countFinalBills() {
        return invoiceRepository.countByStatus(InvoiceStatus.FINAL_BILL);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return invoiceRepository.totalRevenue();
    }

    @Transactional(readOnly = true)
    public BigDecimal getMonthlyRevenue() {
        LocalDateTime firstOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return invoiceRepository.totalRevenueFrom(firstOfMonth);
    }

    // ─── Private Helpers ────────────────────────────────────────────────────

    private Invoice createInvoice(InvoiceFormDto form, InvoiceStatus status, String createdBy) {
        Customer customer = customerRepository.findById(form.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        String prefix = status == InvoiceStatus.QUOTATION ? "QUO" : "BILL";
        String invoiceNumber = generateInvoiceNumber(prefix);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .status(status)
                .notes(form.getNotes())
                .createdBy(createdBy)
                .build();

        if (status == InvoiceStatus.FINAL_BILL) {
            invoice.setConvertedAt(LocalDateTime.now());
        }

        // Build items
        for (InvoiceItemDto itemDto : form.getItems()) {
            if (itemDto.getProductId() == null || itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
                continue;
            }
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemDto.getProductId()));

            BigDecimal price = (itemDto.getSoldAtPrice() != null && itemDto.getSoldAtPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? itemDto.getSoldAtPrice()
                    : product.getCurrentPrice();

            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice)
                    .product(product)
                    .soldAtPrice(price)
                    .quantity(itemDto.getQuantity())
                    .hsnCode(product.getHsnCode())
                    .productName(product.getName())
                    .build();
            item.calculateAmounts();
            invoice.getItems().add(item);
        }

        if (invoice.getItems().isEmpty()) {
            throw new IllegalArgumentException("Invoice must have at least one item.");
        }

        invoice.calculateTotals();
        return invoiceRepository.save(invoice);
    }

    private void deductStockForInvoice(Invoice invoice) {
        for (InvoiceItem item : invoice.getItems()) {
            productService.deductStock(item.getProduct().getId(), item.getQuantity());
        }
    }

    private String generateInvoiceNumber(String prefix) {
        String year = String.valueOf(LocalDateTime.now().getYear()).substring(2);
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        Long maxId = invoiceRepository.findMaxId();
        long nextSeq = (maxId == null ? 0 : maxId) + 1;
        return String.format("%s/%s%s/%04d", prefix, year, month, nextSeq);
    }
}
