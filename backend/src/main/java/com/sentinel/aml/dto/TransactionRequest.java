package com.sentinel.aml.dto;

import com.sentinel.aml.model.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionRequest(@NotNull Long accountId, @NotNull TransactionType transactionType,
        @NotNull @DecimalMin("0.01") BigDecimal amount, @NotBlank String currency,
        @NotNull LocalDateTime transactionTime, String counterparty, String jurisdiction, String channel) {
}
