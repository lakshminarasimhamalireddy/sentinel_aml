package com.sentinel.aml.service;

import com.sentinel.aml.model.*;
import com.sentinel.aml.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DetectionService {
  private static final String THRESHOLD = "LARGE_TRANSACTION", STRUCTURING = "STRUCTURING",
      HIGH_RISK = "HIGH_RISK_JURISDICTION";
  private final TransactionRepository transactions;
  private final AlertRepository alerts;
  private final RuleSettings settings;

  public DetectionService(TransactionRepository transactions, AlertRepository alerts, RuleSettings settings) {
    this.transactions = transactions;
    this.alerts = alerts;
    this.settings = settings;
  }

  @Transactional
  public void evaluate(AmlTransaction tx) {
    if (settings.enabled(THRESHOLD) && tx.getAmountInInr()
        .compareTo(new BigDecimal(settings.value("LARGE_TRANSACTION_THRESHOLD_INR", "830000"))) >= 0)
      createOrUpdate(tx, THRESHOLD, settings.weight(THRESHOLD, 50), "Transaction " + tx.getId() + " is INR "
          + tx.getAmountInInr() + ", above the configured reporting threshold.", List.of(tx));
    if (settings.enabled(HIGH_RISK) && highRisk(tx.getJurisdiction()))
      createOrUpdate(tx, HIGH_RISK, settings.weight(HIGH_RISK, 90),
          "Transaction " + tx.getId() + " involves configured high-risk jurisdiction " + tx.getJurisdiction() + ".",
          List.of(tx));
    if (settings.enabled(STRUCTURING))
      evaluateStructuring(tx);
  }

  private void evaluateStructuring(AmlTransaction tx) {
    LocalDateTime end = tx.getTransactionTime(), start = end.minusHours(24);
    BigDecimal lower = new BigDecimal(settings.value("STRUCTURING_LOWER_INR", "747000")),
        upper = new BigDecimal(settings.value("STRUCTURING_UPPER_INR", "829999"));
    List<AmlTransaction> matches = transactions
        .findByAccountIdAndTransactionTimeBetween(tx.getAccount().getId(), start, end).stream()
        .filter(t -> t.getAmountInInr().compareTo(lower) >= 0 && t.getAmountInInr().compareTo(upper) <= 0).toList();
    int count = Integer.parseInt(settings.value("STRUCTURING_COUNT", "3"));
    if (matches.size() >= count) {
      BigDecimal total = matches.stream().map(AmlTransaction::getAmountInInr).reduce(BigDecimal.ZERO, BigDecimal::add);
      createOrUpdate(tx, STRUCTURING, settings.weight(STRUCTURING, 85), "Structuring suspected: account made "
          + matches.size() + " near-threshold transactions in 24 hours totaling INR " + total + ".", matches);
    }
  }

  private boolean highRisk(String jurisdiction) {
    if (jurisdiction == null)
      return false;
    return Arrays.stream(settings.value("HIGH_RISK_JURISDICTIONS", "IR,KP,SY").split(",")).map(String::trim)
        .anyMatch(j -> j.equalsIgnoreCase(jurisdiction));
  }

  private void createOrUpdate(AmlTransaction tx, String rule, int score, String explanation,
      List<AmlTransaction> evidence) {
    String ids = evidence.stream().map(t -> String.valueOf(t.getId())).collect(Collectors.joining(","));
    Alert alert = alerts.findFirstByAccountIdAndRuleNameAndStatusInOrderByCreatedAtDesc(tx.getAccount().getId(), rule,
        List.of(AlertStatus.OPEN, AlertStatus.UNDER_REVIEW, AlertStatus.ESCALATED)).orElseGet(Alert::new);
    alert.setCustomer(tx.getAccount().getCustomer());
    alert.setAccount(tx.getAccount());
    alert.setRuleName(rule);
    alert.setRiskScore(Math.min(100, score));
    alert.setExplanation(explanation);
    alert.setEvidenceTransactionIds(ids);
    alert.touch();
    alerts.save(alert);
  }
}
