package com.danielflores38153.ms_security.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.danielflores38153.ms_security.Models.Payment;
import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Repositories.PaymentRepository;
import com.danielflores38153.ms_security.Services.ValidatorsService;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final ValidatorsService validatorsService;

    // URL configurable desde application.properties (con valor por defecto)
    @Value("${verifier.url:http://localhost:8081/api/verifier/check}")
    private String verifierUrl;

    public PaymentController(PaymentRepository paymentRepository, ValidatorsService validatorsService) {
        this.paymentRepository = paymentRepository;
        this.restTemplate = new RestTemplate();
        this.validatorsService = validatorsService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestBody Payment payment, HttpServletRequest request) {
        // opcional: ligar el payment al usuario autenticado en vez de confiar en el cliente
        User theUser = validatorsService.getUser(request);
        if (theUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }
        // no asignar el id del usuario al id del pago (crearía conflicto y es semánticamente incorrecto)
        // En su lugar guardamos la referencia al usuario (DBRef) para que la consulta por user._id funcione.
        payment.setUser(theUser);
        payment.setStatus("PENDING");
        Payment saved = paymentRepository.save(payment);

        // Enviar notificación al microservicio verificador usando la URL configurable
        try {
            restTemplate.postForEntity(verifierUrl, saved, Void.class);
        } catch (Exception e) {
            System.out.println("No se pudo notificar al verificador: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Obtener todos los payments del usuario autenticado (usa el token, no acepta userId desde el cliente)
    @GetMapping("/me")
    public ResponseEntity<?> getMyPayments(HttpServletRequest request) {
        User theUser = validatorsService.getUser(request);
        if (theUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
        }

        String userId = theUser.get_id();
        List<Payment> payments = paymentRepository.findByUserId(userId);

        return ResponseEntity.ok(payments);
    }

}
