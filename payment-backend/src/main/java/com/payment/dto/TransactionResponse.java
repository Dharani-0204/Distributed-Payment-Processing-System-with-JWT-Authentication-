package com.payment.dto;

import com.payment.entity.Transaction.TransactionStatus;
import com.payment.entity.Transaction.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Rich transaction response DTO — returned to the frontend.
 * Includes full sender/receiver info and direction relative to the requesting user.
 */
@Data
@Builder
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;

    // DEPOSIT or TRANSFER
    private TransactionType type;

    // PENDING, SUCCESS, or FAILED
    private TransactionStatus status;

    // Unique reference for this transaction
    private String referenceId;

    // When the transaction was created
    private LocalDateTime createdAt;

    // Null for DEPOSIT transactions (no sender — money from outside)
    private UserSummary sender;

    // Always present — who receives the money
    private UserSummary receiver;

    // Direction from the perspective of the currently logged-in user:
    // "IN"  = money came to me (I am the receiver)
    // "OUT" = money went from me (I am the sender)
    private String direction;
}
