package com.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing every financial event in the system.
 * Supports DEPOSIT and TRANSFER types with PENDING → SUCCESS/FAILED lifecycle.
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sender is null for DEPOSIT transactions (money comes from outside)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private BigDecimal amount;

    // Unique reference ID for idempotency — prevents duplicate processing
    @Column(name = "reference_id", unique = true)
    private String referenceId;

    // DEPOSIT = wallet top-up, TRANSFER = user-to-user payment
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // Lifecycle: PENDING → SUCCESS or PENDING → FAILED
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    // Internal fraud flag — not exposed to frontend
    @Column(name = "fraud_flag", nullable = false)
    private boolean fraudFlag = false;

    public enum TransactionType {
        DEPOSIT, TRANSFER
    }

    public enum TransactionStatus {
        PENDING, SUCCESS, FAILED
    }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
