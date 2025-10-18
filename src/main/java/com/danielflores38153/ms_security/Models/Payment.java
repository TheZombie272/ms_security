package com.danielflores38153.ms_security.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Payment {

    @Id
    private String id;
    @DBRef
    private User user;

    private String walletAddress;
    private String transactionHash;
    private Double amount;
    private String currency; // BTC, ETH, USDT...
    private String status; // PENDING, CONFIRMED, FAILED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Payment() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }

}