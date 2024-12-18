package com.danielflores38153.ms_security.Models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@Getter
@Setter
public class UserRole {
    @Id
    private String id;

    @DBRef
    private Role role;
    @DBRef
    private User user;

 
}