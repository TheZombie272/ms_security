package com.danielflores38153.ms_security.Controllers;

import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Repositories.UserRepository;
import com.danielflores38153.ms_security.Services.EncryptionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    @GetMapping("")
    public List<User> getAllUsers() {
        return this.theUserRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable String id) {
        return this.theUserRepository.findById(id).orElse(null);
    }

    @PostMapping("")
    public User createUser(@RequestBody User newUser) {
        newUser.setPassword(theEncryptionService.convertSHA256(newUser.getPassword()));
        return this.theUserRepository.save(newUser);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable String id) {
        User theUser = this.theUserRepository.findById(id).orElse(null);

        if (theUser != null) {
            this.theUserRepository.delete(theUser);
        }else {
            System.out.println("No se puede eliminar el usuario");
        }
    }

    @PutMapping("{id}")
    public User updateUser(@PathVariable String id, @RequestBody User updateUser) {
        User theUser = this.theUserRepository.findById(id).orElse(null);
        if (theUser != null) {
            theUser.setName(updateUser.getName());
            theUser.setEmail(updateUser.getEmail());
            theUser.setPassword(theEncryptionService.convertSHA256(updateUser.getPassword()));
            return this.theUserRepository.save(theUser);
        }
        return null;
    }
}
