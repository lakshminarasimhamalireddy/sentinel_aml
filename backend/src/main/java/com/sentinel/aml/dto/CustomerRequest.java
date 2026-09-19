package com.sentinel.aml.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(@NotBlank String fullName, @NotBlank String customerReference, String riskRating) {
}
