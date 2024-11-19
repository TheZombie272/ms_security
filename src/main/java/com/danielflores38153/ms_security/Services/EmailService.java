package com.danielflores38153.ms_security.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    private RestTemplate restTemplate;

    @Autowired
    public EmailService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void sendEmail(String email, String subject, String body) {
        try {
            final String url = "http://localhost:5000/send-email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = String.format("{\"email\":\"%s\", \"subject\":\"%s\", \"body\":\"%s\"}", email, subject, body);
            HttpEntity<String> request = new HttpEntity<>(json, headers);

            // Llamada a la API
            String response = this.restTemplate.postForObject(url, request, String.class);
            System.out.println("Response from email API: " + response);
        } catch (Exception e) {
            // Capturar y mostrar cualquier error
            e.printStackTrace();
            System.out.println("Error enviando el correo: " + e.getMessage());
        }
    }
}