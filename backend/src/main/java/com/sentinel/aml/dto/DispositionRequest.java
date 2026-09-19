package com.sentinel.aml.dto;

import com.sentinel.aml.model.AlertStatus;
import jakarta.validation.constraints.*;

public record DispositionRequest(@NotNull AlertStatus status, @NotBlank String analystName,
        @NotBlank String dispositionReason) {
}
