package com.danielflores38153.ms_security.Models;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document
@Data
@Getter
@Setter
public class Session {
    @Id
    private String _id;
    private String token2FA;
    private String token;
    private boolean used;
    private int expiresAfter = 1800;
    @DBRef
    private User user;
    private LocalDateTime createDateTime;
    private LocalDateTime expirationDateTime;

    public Session(String token, User user){
        this.token = token;
        this.used = false;
        this.user = user;
        this.createDateTime = LocalDateTime.now();
        this.expirationDateTime = LocalDateTime.now().plusSeconds(1800);
    }

}