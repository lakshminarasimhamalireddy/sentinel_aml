package com.sentinel.aml.repository;

import com.sentinel.aml.model.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<AmlTransaction, Long> {
    List<AmlTransaction> findByAccountIdAndTransactionTimeBetween(Long accountId, LocalDateTime from, LocalDateTime to);

    List<AmlTransaction> findByAccountCustomerIdAndTransactionTimeBetween(Long customerId, LocalDateTime from,
            LocalDateTime to);
}
