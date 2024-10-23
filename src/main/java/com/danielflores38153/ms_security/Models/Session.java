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

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getToken2FA() {
        return token2FA;
    }

    public void setToken2FA(String token2FA) {
        this.token2FA = token2FA;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getExpiresAfter() {
        return expiresAfter;
    }

    public void setExpiresAfter(int expiresAfter) {
        this.expiresAfter = expiresAfter;
    }

    public LocalDateTime getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(LocalDateTime createDateTime) {
        this.createDateTime = createDateTime;
    }

    public LocalDateTime getExpirationDateTime() {
        return expirationDateTime;
    }

    public void setExpirationDateTime(LocalDateTime expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
    }

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

    public Session(){}

}