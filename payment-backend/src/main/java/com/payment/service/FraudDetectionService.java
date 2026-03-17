package com.payment.service;

import com.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;

    private static final BigDecimal LARGE_TRANSACTION_THRESHOLD = new BigDecimal("50000");
    private static final int MAX_TRANSACTIONS_PER_MINUTE = 5;

    public boolean isFraudulent(Long senderId, BigDecimal amount) {
        // 1. Check for abnormally large transactions
        if (amount.compareTo(LARGE_TRANSACTION_THRESHOLD) > 0) {
            return true;
        }

        // 2. Velocity check: More than X transactions in the last minute
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long recentTxCount = transactionRepository.countBySenderIdAndTimestampAfter(senderId, oneMinuteAgo);
        
        if (recentTxCount >= MAX_TRANSACTIONS_PER_MINUTE) {
            return true;
        }

        return false;
    }
}
