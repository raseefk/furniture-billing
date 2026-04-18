package com.furnitureshop.repository;

import com.furnitureshop.model.Invoice;
import com.furnitureshop.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByStatusOrderByCreatedAtDesc(InvoiceStatus status);
    List<Invoice> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT i FROM Invoice i WHERE i.status = :status AND " +
           "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "i.customer.phone LIKE CONCAT('%', :q, '%'))")
    List<Invoice> searchByStatus(@Param("status") InvoiceStatus status, @Param("q") String query);

    @Query("SELECT i FROM Invoice i WHERE " +
           "(LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "i.customer.phone LIKE CONCAT('%', :q, '%')) " +
           "ORDER BY i.createdAt DESC")
    List<Invoice> searchAll(@Param("q") String query);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.status = :status")
    long countByStatus(@Param("status") InvoiceStatus status);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.status = 'FINAL_BILL' AND i.convertedAt >= :from")
    BigDecimal totalRevenueFrom(@Param("from") LocalDateTime from);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.status = 'FINAL_BILL'")
    BigDecimal totalRevenue();

    // For invoice number generation
    @Query("SELECT MAX(i.id) FROM Invoice i")
    Long findMaxId();

    List<Invoice> findTop10ByStatusOrderByCreatedAtDesc(InvoiceStatus status);
    List<Invoice> findTop10ByOrderByCreatedAtDesc();
}
