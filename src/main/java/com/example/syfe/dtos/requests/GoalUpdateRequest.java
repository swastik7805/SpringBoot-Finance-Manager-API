package com.example.syfe.dtos.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class GoalUpdateRequest {

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be a positive value")
    private BigDecimal targetAmount;

    @NotNull(message = "Target date is required")
    private LocalDate targetDate;
}
