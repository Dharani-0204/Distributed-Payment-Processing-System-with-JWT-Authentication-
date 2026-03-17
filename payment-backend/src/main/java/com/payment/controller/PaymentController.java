package com.payment.controller;

import com.payment.dto.TransactionResponse;
import com.payment.dto.TransferRequest;
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
 * Handles money transfer between users.
 * Sender identity comes from JWT — no senderId needed in the request body.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Transfer money to another user identified by their email.
     * POST /api/payments/transfer
     * Body: { "receiverEmail": "bob@example.com", "amount": 100.00, "referenceId": "TXN-abc" }
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {

        String senderEmail = userDetails.getUsername();
        return ResponseEntity.ok(paymentService.transfer(senderEmail, request));
    }
}
