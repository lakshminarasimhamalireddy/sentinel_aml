package com.sentinel.aml.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;
    @Column(unique = true, nullable = false)
    private String customerReference;
    private String riskRating = "MEDIUM";
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String v) {
        fullName = v;
    }

    public String getCustomerReference() {
        return customerReference;
    }

    public void setCustomerReference(String v) {
        customerReference = v;
    }

    public String getRiskRating() {
        return riskRating;
    }

    public void setRiskRating(String v) {
        riskRating = v;
    }
}
