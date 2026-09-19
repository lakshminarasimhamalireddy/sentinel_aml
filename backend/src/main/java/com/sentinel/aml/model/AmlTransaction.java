package com.sentinel.aml.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aml_transactions")
public class AmlTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id")
    private Account account;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountInInr;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private LocalDateTime transactionTime;
    private String counterparty;
    private String jurisdiction;
    private String channel;

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account v) {
        account = v;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType v) {
        transactionType = v;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal v) {
        amount = v;
    }

    public BigDecimal getAmountInInr() {
        return amountInInr;
    }

    public void setAmountInInr(BigDecimal v) {
        amountInInr = v;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String v) {
        currency = v;
    }

    public LocalDateTime getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(LocalDateTime v) {
        transactionTime = v;
    }

    public String getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(String v) {
        counterparty = v;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String v) {
        jurisdiction = v;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String v) {
        channel = v;
    }
}
