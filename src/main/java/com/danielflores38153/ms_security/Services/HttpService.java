package com.danielflores38153.ms_security.Services;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class HttpService {
    @Value("${url.notification}")
    private String urlNotifications;


    public ResponseEntity<String> postNotification(String route, String body) {
		HttpHeaders headers = new HttpHeaders(); // Headers para la petición
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers); // Body de la petición
		
        ResponseEntity<String> response;
        try {
            URI urlNotifications = new URI(this.urlNotifications);
			RestTemplate restTemplate = new RestTemplate();
            response = restTemplate.postForEntity(urlNotifications + route, httpEntity, String.class);
        } catch (URISyntaxException e) {
            response = new ResponseEntity<>(e.toString(), headers, HttpStatus.FAILED_DEPENDENCY);
            e.printStackTrace();
        }
		
        return response;
    }


}