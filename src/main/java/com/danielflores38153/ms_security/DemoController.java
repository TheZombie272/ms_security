package com.danielflores38153.ms_security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class DemoController {
    @GetMapping
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("hola mi hermano");
    }
    @GetMapping("/user")
    public Principal user(Principal principal) {
        System.out.println("usuario : " + principal.getName());
        return principal;
    }
}
