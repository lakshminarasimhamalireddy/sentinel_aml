package com.sentinel.aml.dto;

import jakarta.validation.constraints.*;

public record AccountRequest(@NotNull Long customerId, @NotBlank String accountNumber, @NotBlank String accountType,
        String currency) {
}
