package com.payment.controller;

import com.payment.dto.TransactionResponse;
import com.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides transaction history and summary statistics for the logged-in user.
 * All data is scoped to the authenticated user via JWT — no userId in the path.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final PaymentService paymentService;

    /**
     * Get full transaction history for the current user.
     * GET /api/transactions
     * Returns both sent and received transactions, sorted newest first.
     * Each item includes direction (IN/OUT), sender/receiver info, status.
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        return ResponseEntity.ok(paymentService.getTransactions(email));
    }

    /**
     * Get wallet statistics: total sent, total received.
     * GET /api/transactions/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, BigDecimal>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        Map<String, BigDecimal> stats = new HashMap<>();
        stats.put("totalSent", paymentService.getTotalSent(email));
        stats.put("totalReceived", paymentService.getTotalReceived(email));
        return ResponseEntity.ok(stats);
    }
}
