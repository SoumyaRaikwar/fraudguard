package com.fraudguard.utils;

import com.fraudguard.dto.TransactionCreateRequest;
import com.fraudguard.dto.UserCreateRequest;
import com.fraudguard.entity.Transaction;
import com.fraudguard.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TestDataBuilder {

    public static UserCreateRequest.UserCreateRequestBuilder defaultUserCreateRequest() {
        return UserCreateRequest.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password123");
    }

    public static User.UserBuilder defaultUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashed-password")
                .isActive(true)
                .riskProfile("LOW")
                .createdAt(LocalDateTime.now());
    }

    public static TransactionCreateRequest.TransactionCreateRequestBuilder defaultTransactionCreateRequest() {
        return TransactionCreateRequest.builder()
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .ipAddress("192.168.1.1")
                .deviceFingerprint("device123");
    }

    public static Transaction.TransactionBuilder defaultTransaction() {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .currency("USD")
                .merchantId("merchant-001")
                .transactionType("PURCHASE")
                .status("APPROVED")
                .fraudScore(new BigDecimal("0.15"))
                .riskLevel("LOW")
                .ipAddress("192.168.1.1")
                .deviceFingerprint("device123")
                .createdAt(LocalDateTime.now());
    }

    public static Transaction highRiskTransaction() {
        return defaultTransaction()
                .amount(new BigDecimal("10000.00"))
                .fraudScore(new BigDecimal("0.95"))
                .riskLevel("HIGH")
                .status("DECLINED")
                .build();
    }

    public static User highRiskUser() {
        return defaultUser()
                .email("highrisk@example.com")
                .riskProfile("HIGH")
                .build();
    }
}
