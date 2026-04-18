package com.furnitureshop.repository;

import com.furnitureshop.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByActiveTrueOrderByNameAsc();
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByGstNumber(String gstNumber);

    @Query("SELECT c FROM Customer c WHERE c.active = true AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "c.phone LIKE CONCAT('%', :q, '%') OR " +
           "LOWER(c.gstNumber) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Customer> search(@Param("q") String query);
}
