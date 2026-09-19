package com.sentinel.aml.service;

import com.sentinel.aml.model.RuleConfig;
import com.sentinel.aml.repository.RuleConfigRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RuleSettings {
    private final RuleConfigRepository repository;

    public RuleSettings(RuleConfigRepository repository) {
        this.repository = repository;
    }

    public boolean enabled(String key) {
        return repository.findById(key).map(RuleConfig::isEnabled).orElse(true);
    }

    public int weight(String key, int fallback) {
        return repository.findById(key).map(RuleConfig::getRiskWeight).filter(v -> v > 0).orElse(fallback);
    }

    public String value(String key, String fallback) {
        return repository.findById(key).map(RuleConfig::getValue).filter(v -> v != null && !v.isBlank())
                .orElse(fallback);
    }

    public List<RuleConfig> all() {
        return repository.findAll();
    }

    public RuleConfig save(RuleConfig config) {
        return repository.save(config);
    }
}
