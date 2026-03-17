package com.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private java.math.BigDecimal balance;
    private String createdAt;
}
