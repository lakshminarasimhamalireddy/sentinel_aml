package com.sentinel.aml.model;

import jakarta.persistence.*;

@Entity
public class RuleConfig {
    @Id
    @Column(name = "rule_key")
    private String key;
    private boolean enabled = true;
    private String value;
    private int riskWeight;

    public String getKey() {
        return key;
    }

    public void setKey(String v) {
        key = v;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean v) {
        enabled = v;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String v) {
        value = v;
    }

    public int getRiskWeight() {
        return riskWeight;
    }

    public void setRiskWeight(int v) {
        riskWeight = v;
    }
}
