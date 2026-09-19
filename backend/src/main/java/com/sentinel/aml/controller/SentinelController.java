package com.sentinel.aml.controller;

import com.sentinel.aml.dto.*;
import com.sentinel.aml.model.*;
import com.sentinel.aml.repository.*;
import com.sentinel.aml.service.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.math.*;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class SentinelController {
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final AlertRepository alerts;
    private final RuleSettings rules;
    private final DetectionService detection;

    public SentinelController(CustomerRepository customers, AccountRepository accounts,
            TransactionRepository transactions, AlertRepository alerts, RuleSettings rules,
            DetectionService detection) {
        this.customers = customers;
        this.accounts = accounts;
        this.transactions = transactions;
        this.alerts = alerts;
        this.rules = rules;
        this.detection = detection;
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public Customer createCustomer(@Valid @RequestBody CustomerRequest req) {
        Customer c = new Customer();
        c.setFullName(req.fullName());
        c.setCustomerReference(req.customerReference());
        if (req.riskRating() != null)
            c.setRiskRating(req.riskRating());
        return customers.save(c);
    }

    @GetMapping("/customers")
    public List<Customer> customers() {
        return customers.findAll();
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public Account createAccount(@Valid @RequestBody AccountRequest req) {
        Customer customer = customers.findById(req.customerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));
        Account a = new Account();
        a.setCustomer(customer);
        a.setAccountNumber(req.accountNumber());
        a.setAccountType(req.accountType());
        if (req.currency() != null)
            a.setCurrency(req.currency());
        return accounts.save(a);
    }

    @GetMapping("/accounts")
    public List<Account> accounts() {
        return accounts.findAll();
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ingest a transaction and immediately evaluate AML rules")
    public AmlTransaction ingest(@Valid @RequestBody TransactionRequest req) {
        Account account = accounts.findById(req.accountId())
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        AmlTransaction tx = new AmlTransaction();
        tx.setAccount(account);
        tx.setTransactionType(req.transactionType());
        tx.setAmount(req.amount());
        tx.setCurrency(req.currency().toUpperCase());
        tx.setAmountInInr(normalize(req.amount(), req.currency()));
        tx.setTransactionTime(req.transactionTime());
        tx.setCounterparty(req.counterparty());
        tx.setJurisdiction(req.jurisdiction());
        tx.setChannel(req.channel());
        tx = transactions.save(tx);
        detection.evaluate(tx);
        return tx;
    }

    @GetMapping("/transactions")
    public List<AmlTransaction> transactionList() {
        return transactions.findAll();
    }

    @GetMapping("/alerts")
    public List<Alert> alertList() {
        return alerts.findAllByOrderByRiskScoreDescCreatedAtDesc();
    }

    @GetMapping("/alerts/{id}")
    public Alert alert(@PathVariable Long id) {
        return alerts.findById(id).orElseThrow(() -> new NoSuchElementException("Alert not found"));
    }

    @PatchMapping("/alerts/{id}/status")
    public Alert dispose(@PathVariable Long id, @Valid @RequestBody DispositionRequest req) {
        Alert a = alerts.findById(id).orElseThrow(() -> new NoSuchElementException("Alert not found"));
        a.setStatus(req.status());
        a.setAnalystName(req.analystName());
        a.setDispositionReason(req.dispositionReason());
        a.touch();
        return alerts.save(a);
    }

    @GetMapping("/admin/rules")
    public List<RuleConfig> rules() {
        return rules.all();
    }

    @PutMapping("/admin/rules/{key}")
    public RuleConfig rule(@PathVariable String key, @RequestBody RuleConfig config) {
        config.setKey(key);
        return rules.save(config);
    }

    private BigDecimal normalize(BigDecimal amount, String currency) {
        return switch (currency.toUpperCase()) {
            case "INR" -> amount;
            case "USD" -> amount.multiply(new BigDecimal("83"));
            case "EUR" -> amount.multiply(new BigDecimal("90"));
            default -> throw new IllegalArgumentException("Unsupported currency: " + currency);
        };
    }
}
