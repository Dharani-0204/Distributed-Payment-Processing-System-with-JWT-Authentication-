package com.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private java.math.BigDecimal balance = java.math.BigDecimal.ZERO;

    public java.math.BigDecimal getBalance() {
        return balance == null ? java.math.BigDecimal.ZERO : balance;
    }

    @Column(name = "created_at", nullable = true, updatable = false)
    private LocalDateTime createdAt;
    
    public LocalDateTime getCreatedAt() {
        return createdAt == null ? LocalDateTime.now() : createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
