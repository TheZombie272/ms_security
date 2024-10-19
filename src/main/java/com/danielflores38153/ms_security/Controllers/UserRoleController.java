package com.danielflores38153.ms_security.Controllers;

import com.danielflores38153.ms_security.Models.Role;
import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Models.UserRole;
import com.danielflores38153.ms_security.Repositories.RoleRepository;
import com.danielflores38153.ms_security.Repositories.UserRepository;
import com.danielflores38153.ms_security.Repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/user_roles")
public class UserRoleController {

    @Autowired
    private UserRoleRepository theUserRoleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;


    @GetMapping("")
    public List<UserRole> getAllUserRole() {
        return this.theUserRoleRepository.findAll();

    }

    @GetMapping("/{id}")
    public UserRole getUserRoleById(@PathVariable String id) {
        return this.theUserRoleRepository.findById(id).orElse(null);
    }

    @GetMapping("/user/{userId}")
    public List<UserRole> getUserRolesByUserId(@PathVariable String userId) {
        return this.theUserRoleRepository.getUserRolesByUserId(userId);
    }

    @GetMapping("/role/{roleId}")
    public List<UserRole> getUserRolesByRoleId(@PathVariable String roleId) {
        return this.theUserRoleRepository.getUserRolesByRoleId(roleId);
    }

    @PostMapping("user/{userId}/role/{roleId}")
    public UserRole createUserRole( @PathVariable String userId, @PathVariable String roleId) {
        User theUser = this.userRepository.findById(userId).orElse(null);
        Role theRole = this.roleRepository.findById(roleId).orElse(null);

        if (theUser != null && theRole != null) {
            UserRole theUserRole = new UserRole();
            theUserRole.setUser(theUser);
            theUserRole.setRole(theRole);

            return theUserRoleRepository.save(theUserRole);
        }
        return null;
    }

    @DeleteMapping("{id}")
    public void deletUserRole(@PathVariable String id) {
        UserRole theUserRole = this.theUserRoleRepository.findById(id).orElse(null);

        if (theUserRole != null) {
            this.theUserRoleRepository.delete(theUserRole);
        }else {
            System.out.println("No se puede eliminar el usuario");
        }
    }

    @PutMapping("{id}")
    public UserRole updateUser(@PathVariable String id, @RequestBody UserRole updateUserRole) {
        UserRole theUserRole = this.theUserRoleRepository.findById(id).orElse(null);
        User theUser = this.userRepository.findById(updateUserRole.getUser().get_id()).orElse(null);
        Role theRole = this.roleRepository.findById(updateUserRole.getRole().get_id()).orElse(null);

        if (theUserRole != null && theRole != null && theUser != null) {
            theUserRole.setUser(theUser);
            theUserRole.setRole(theRole);
            return this.theUserRoleRepository.save(theUserRole);
        }
        return null;
    }
}