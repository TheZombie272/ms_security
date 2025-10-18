package com.danielflores38153.ms_security.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.danielflores38153.ms_security.Models.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByTransactionHash(String transactionHash);
    List<Payment> findByUserId(String userId);
}
