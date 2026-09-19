package com.sentinel.aml.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @Column(unique = true, nullable = false)
    private String accountNumber;
    private String accountType;
    private String currency = "INR";
    private LocalDate openingDate = LocalDate.now();

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer v) {
        customer = v;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String v) {
        accountNumber = v;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String v) {
        accountType = v;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String v) {
        currency = v;
    }
}
