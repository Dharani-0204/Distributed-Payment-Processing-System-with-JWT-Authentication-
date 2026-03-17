package com.payment.controller;

import com.payment.dto.DepositRequest;
import com.payment.dto.TransactionResponse;
import com.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles wallet deposit operations.
 * The current user is extracted from the JWT token — no need to pass userId.
 */
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final PaymentService paymentService;

    /**
     * Deposit money into the authenticated user's wallet.
     * POST /api/wallet/deposit
     * Body: { "amount": 500.00 }
     */
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DepositRequest request) {

        // Extract email from JWT, not from the request body
        String email = userDetails.getUsername();
        return ResponseEntity.ok(paymentService.deposit(email, request));
    }
}
