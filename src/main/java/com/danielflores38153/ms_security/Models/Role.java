package com.danielflores38153.ms_security.Models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@Getter
@Setter
public class Role {
    @Id
    private String _id;
    private String name;
    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
// hacer un empoin que retorne el rol que utiliza mas beses el login