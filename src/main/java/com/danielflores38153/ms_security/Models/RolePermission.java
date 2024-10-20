package com.danielflores38153.ms_security.Models;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Getter
@Setter
public class RolePermission {
    @Id
    private String id;

    @DBRef
    private Role role;
    @DBRef
    private Permission permission;
}
