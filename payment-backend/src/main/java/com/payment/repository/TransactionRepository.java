package com.payment.repository;

import com.payment.entity.Transaction;
import com.payment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Fetch all transactions where the user is either sender or receiver.
     * Sorted by timestamp descending (newest first) — used for transaction history.
     */
    List<Transaction> findBySenderOrReceiverOrderByTimestampDesc(User sender, User receiver);

    /**
     * Used by FraudDetectionService to count recent outgoing transfers.
     */
    long countBySenderIdAndTimestampAfter(Long senderId, LocalDateTime timestamp);

    /**
     * Idempotency check — prevents processing the same reference twice.
     */
    boolean existsByReferenceId(String referenceId);
}
