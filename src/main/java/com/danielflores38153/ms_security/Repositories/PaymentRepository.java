package com.danielflores38153.ms_security.Repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.danielflores38153.ms_security.Models.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {
    Optional<Payment> findByTransactionHash(String transactionHash);

    // El modelo User usa el campo "_id" (notar el guion bajo). La derivación automática
    // fallaba porque Spring buscaba "user.id". Usamos una consulta explícita que
    // consulta por user._id para evitar renombrar modelos.
    @Query("{ 'user._id': ?0 }")
    List<Payment> findByUserId(String userId);
}
