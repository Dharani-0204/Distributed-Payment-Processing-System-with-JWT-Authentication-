package com.payment.service;

import com.payment.dto.DepositRequest;
import com.payment.dto.TransactionResponse;
import com.payment.dto.TransferRequest;
import com.payment.dto.UserSummary;
import com.payment.entity.Transaction;
import com.payment.entity.Transaction.TransactionStatus;
import com.payment.entity.Transaction.TransactionType;
import com.payment.entity.User;
import com.payment.repository.TransactionRepository;
import com.payment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Core payment service — handles deposits, transfers, and transaction history.
 * All methods accept the authenticated user's email (extracted from JWT by the controller).
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    // ─────────────────── DEPOSIT ───────────────────

    /**
     * Deposits money into the currently logged-in user's wallet.
     * Creates a DEPOSIT transaction with status SUCCESS.
     *
     * @param email   Email extracted from the JWT token
     * @param request Contains only the amount
     */
    @Transactional
    public TransactionResponse deposit(String email, DepositRequest request) {
        // Look up the authenticated user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Add the deposit amount to their balance
        user.setBalance(user.getBalance().add(request.getAmount()));
        userRepository.save(user);

        // Record the deposit as a completed transaction (no sender — money from outside)
        Transaction transaction = new Transaction();
        transaction.setReceiver(user);
        transaction.setSender(null); // Deposits have no sender
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setReferenceId("DEP-" + System.currentTimeMillis() + "-" + user.getId());

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved, user);
    }

    // ─────────────────── TRANSFER ───────────────────

    /**
     * Transfers money from the logged-in user to another user identified by email.
     * Flow: Create PENDING → validate → deduct/credit → mark SUCCESS or FAILED.
     *
     * @param senderEmail Email of the sender (from JWT)
     * @param request     Contains receiverEmail, amount, referenceId
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse transfer(String senderEmail, TransferRequest request) {
        // ─── Idempotency check ───
        if (transactionRepository.existsByReferenceId(request.getReferenceId())) {
            throw new IllegalArgumentException("Duplicate transaction reference ID: " + request.getReferenceId());
        }

        // ─── Load sender (from JWT) ───
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));

        // ─── Load receiver (by email from request) ───
        User receiver = userRepository.findByEmail(request.getReceiverEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Receiver not found with email: " + request.getReceiverEmail()));

        // ─── Business rule: cannot transfer to self ───
        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot transfer money to yourself");
        }

        // ─── Step 1: Create transaction as PENDING ───
        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setReferenceId(request.getReferenceId());
        transaction.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        // ─── Step 2: Validate balance ───
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            // Mark FAILED and save before throwing so the record is preserved
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new IllegalArgumentException("Insufficient balance. Available: ₹" + sender.getBalance());
        }

        // ─── Step 3: Fraud detection ───
        boolean isFraud = fraudDetectionService.isFraudulent(sender.getId(), request.getAmount());
        if (isFraud) {
            transaction.setFraudFlag(true);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw new IllegalArgumentException("Transaction flagged as suspicious and blocked");
        }

        // ─── Step 4: Perform transfer ───
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));
        userRepository.save(sender);
        userRepository.save(receiver);

        // ─── Step 5: Mark SUCCESS ───
        transaction.setStatus(TransactionStatus.SUCCESS);
        Transaction saved = transactionRepository.save(transaction);

        return mapToResponse(saved, sender);
    }

    // ─────────────────── HISTORY ───────────────────

    /**
     * Returns transaction history for the logged-in user, sorted newest first.
     * Each transaction includes direction: "IN" or "OUT" relative to this user.
     *
     * @param email Email from JWT
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return transactionRepository
                .findBySenderOrReceiverOrderByTimestampDesc(user, user)
                .stream()
                .map(tx -> mapToResponse(tx, user))
                .collect(Collectors.toList());
    }

    // ─────────────────── STATISTICS ───────────────────

    /**
     * Computes total amount sent by the user (OUT direction, SUCCESS only).
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalSent(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return transactionRepository
                .findBySenderOrReceiverOrderByTimestampDesc(user, user)
                .stream()
                .filter(tx -> tx.getSender() != null
                        && tx.getSender().getId().equals(user.getId())
                        && tx.getStatus() == TransactionStatus.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Computes total amount received by the user (IN direction, SUCCESS only).
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalReceived(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return transactionRepository
                .findBySenderOrReceiverOrderByTimestampDesc(user, user)
                .stream()
                .filter(tx -> tx.getReceiver().getId().equals(user.getId())
                        && tx.getStatus() == TransactionStatus.SUCCESS)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ─────────────────── MAPPER ───────────────────

    /**
     * Maps a Transaction entity to a TransactionResponse DTO.
     * Determines direction (IN/OUT) based on the current user.
     *
     * @param tx          The transaction to map
     * @param currentUser The authenticated user making the request
     */
    private TransactionResponse mapToResponse(Transaction tx, User currentUser) {
        // Build sender summary (null for deposits)
        UserSummary senderSummary = null;
        if (tx.getSender() != null) {
            senderSummary = UserSummary.builder()
                    .id(tx.getSender().getId())
                    .name(tx.getSender().getName())
                    .email(tx.getSender().getEmail())
                    .build();
        }

        // Build receiver summary (always present)
        UserSummary receiverSummary = UserSummary.builder()
                .id(tx.getReceiver().getId())
                .name(tx.getReceiver().getName())
                .email(tx.getReceiver().getEmail())
                .build();

        // Determine direction from the current user's perspective
        // DEPOSIT is always IN; for TRANSFER, check if we are sender or receiver
        String direction;
        if (tx.getType() == TransactionType.DEPOSIT) {
            direction = "IN";
        } else if (tx.getSender() != null && tx.getSender().getId().equals(currentUser.getId())) {
            direction = "OUT"; // We sent the money
        } else {
            direction = "IN";  // We received the money
        }

        return TransactionResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .status(tx.getStatus())
                .referenceId(tx.getReferenceId())
                .createdAt(tx.getTimestamp())
                .sender(senderSummary)
                .receiver(receiverSummary)
                .direction(direction)
                .build();
    }
}
