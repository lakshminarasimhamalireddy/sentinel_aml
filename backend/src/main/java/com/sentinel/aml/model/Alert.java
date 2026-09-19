package com.sentinel.aml.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "aml_alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Customer customer;
    @ManyToOne(optional = false)
    private Account account;
    @Column(nullable = false)
    private String ruleName;
    private int riskScore;
    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.OPEN;
    @Column(length = 3000)
    private String explanation;
    @Column(length = 2000)
    private String evidenceTransactionIds;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String analystName;
    private String dispositionReason;

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer v) {
        customer = v;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account v) {
        account = v;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String v) {
        ruleName = v;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int v) {
        riskScore = v;
    }

    public AlertStatus getStatus() {
        return status;
    }

    public void setStatus(AlertStatus v) {
        status = v;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String v) {
        explanation = v;
    }

    public String getEvidenceTransactionIds() {
        return evidenceTransactionIds;
    }

    public void setEvidenceTransactionIds(String v) {
        evidenceTransactionIds = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getAnalystName() {
        return analystName;
    }

    public void setAnalystName(String v) {
        analystName = v;
    }

    public String getDispositionReason() {
        return dispositionReason;
    }

    public void setDispositionReason(String v) {
        dispositionReason = v;
    }

    public void touch() {
        updatedAt = LocalDateTime.now();
    }
}
