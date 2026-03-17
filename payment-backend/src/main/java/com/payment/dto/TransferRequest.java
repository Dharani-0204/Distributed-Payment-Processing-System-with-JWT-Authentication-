package com.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for transferring money to another user.
 * NOTE: senderId is NOT here — sender is extracted from the JWT token automatically.
 */
@Data
public class TransferRequest {

    // Receiver identified by email (more user-friendly than ID)
    @NotBlank(message = "Receiver email is required")
    @Email(message = "Please provide a valid email address")
    private String receiverEmail;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Reference ID is required")
    private String referenceId;
}
