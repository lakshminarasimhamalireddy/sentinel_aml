package com.sentinel.aml.repository;

import com.sentinel.aml.model.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findAllByOrderByRiskScoreDescCreatedAtDesc();

    Optional<Alert> findFirstByAccountIdAndRuleNameAndStatusInOrderByCreatedAtDesc(Long accountId, String ruleName,
            Collection<AlertStatus> statuses);
}
