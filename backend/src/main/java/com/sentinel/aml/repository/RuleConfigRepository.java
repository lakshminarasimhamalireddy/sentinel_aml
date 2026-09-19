package com.sentinel.aml.repository;

import com.sentinel.aml.model.RuleConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleConfigRepository extends JpaRepository<RuleConfig, String> {
}
