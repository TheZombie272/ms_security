package com.danielflores38153.ms_security.Models;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;

@Getter
@Setter
public class Permission {
    @Id
    private String _id;

    private String url;
    private String method;

}