package com.sentinel.aml.repository;

import com.sentinel.aml.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByCustomerReference(String customerReference);
}
