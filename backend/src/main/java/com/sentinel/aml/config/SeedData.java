package com.sentinel.aml.config;

import com.sentinel.aml.model.Account;
import com.sentinel.aml.model.Customer;
import com.sentinel.aml.model.RuleConfig;
import com.sentinel.aml.repository.AccountRepository;
import com.sentinel.aml.repository.CustomerRepository;
import com.sentinel.aml.repository.RuleConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SeedData {
    @Bean
    CommandLineRunner seed(CustomerRepository customers, AccountRepository accounts, RuleConfigRepository rules) {
        return args -> {
            if (rules.count() == 0) {
                addRule(rules, "LARGE_TRANSACTION", true, "", 50);
                addRule(rules, "LARGE_TRANSACTION_THRESHOLD_INR", true, "830000", 0);
                addRule(rules, "STRUCTURING", true, "", 85);
                addRule(rules, "STRUCTURING_LOWER_INR", true, "747000", 0);
                addRule(rules, "STRUCTURING_UPPER_INR", true, "829999", 0);
                addRule(rules, "STRUCTURING_COUNT", true, "3", 0);
                addRule(rules, "HIGH_RISK_JURISDICTION", true, "", 90);
                addRule(rules, "HIGH_RISK_JURISDICTIONS", true, "IR,KP,SY", 0);
            }
            Path data = Paths.get("..", "BlrAzenioJavahackathon").toAbsolutePath().normalize();
            importCustomers(data.resolve("customers.csv"), customers);
            importAccounts(data.resolve("accounts.csv"), customers, accounts);
        };
    }

    private void importCustomers(Path csv, CustomerRepository customers) throws IOException {
        requireFile(csv);
        try (var lines = Files.lines(csv)) {
            lines.skip(1).map(line -> line.split(",", -1)).forEach(row -> {
                if (!customers.existsByCustomerReference(row[0])) {
                    Customer customer = new Customer();
                    customer.setCustomerReference(row[0]);
                    customer.setFullName(row[1] + " " + row[2]);
                    customer.setRiskRating(emptyOr(row[20], "MEDIUM"));
                    customers.save(customer);
                }
            });
        }
    }

    private void importAccounts(Path csv, CustomerRepository customers, AccountRepository accounts) throws IOException {
        requireFile(csv);
        Map<String, Customer> byReference = new HashMap<>();
        customers.findAll().forEach(customer -> byReference.put(customer.getCustomerReference(), customer));
        try (var lines = Files.lines(csv)) {
            lines.skip(1).map(line -> line.split(",", -1)).forEach(row -> {
                if (!accounts.existsByAccountNumber(row[0])) {
                    Customer customer = byReference.get(row[1]);
                    if (customer == null) throw new IllegalStateException("Account " + row[0] + " references missing customer " + row[1]);
                    Account account = new Account();
                    account.setAccountNumber(row[0]);
                    account.setCustomer(customer);
                    account.setAccountType(row[2]);
                    account.setCurrency(emptyOr(row[4], "INR"));
                    accounts.save(account);
                }
            });
        }
    }

    private void requireFile(Path csv) {
        if (!Files.exists(csv)) throw new IllegalStateException("Required seed file not found: " + csv);
    }

    private String emptyOr(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }

    private void addRule(RuleConfigRepository repository, String key, boolean enabled, String value, int weight) {
        RuleConfig rule = new RuleConfig();
        rule.setKey(key); rule.setEnabled(enabled); rule.setValue(value); rule.setRiskWeight(weight);
        repository.save(rule);
    }
}
