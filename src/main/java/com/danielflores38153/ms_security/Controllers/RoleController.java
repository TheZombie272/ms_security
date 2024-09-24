package com.danielflores38153.ms_security.Controllers;

import com.danielflores38153.ms_security.Models.Role;
import com.danielflores38153.ms_security.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleRepository theRoleRepository;

    @GetMapping("")
    public List<Role> getAllRoles() {
        return this.theRoleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Role getRoleById(@PathVariable String id) {
        return this.theRoleRepository.findById(id).orElse(null);
    }

    @PostMapping("")
    public Role createRole(@RequestBody Role newRole) {
        return this.theRoleRepository.save(newRole);
    }

    @DeleteMapping("{id}")
    public void deleteUser(@PathVariable String id) {
        Role theUser = this.theRoleRepository.findById(id).orElse(null);

        if (theUser != null) {
            this.theRoleRepository.delete(theUser);
        }else {
            System.out.println("No se puede eliminar el usuario");
        }
    }

    @PutMapping("{id}")
    public Role updateUser(@PathVariable String id, @RequestBody Role updatedRole) {
        Role theRole = this.theRoleRepository.findById(id).orElse(null);
        if (theRole != null) {
            theRole.setName(updatedRole.getName());
            theRole.setDescription(updatedRole.getDescription());
            return this.theRoleRepository.save(theRole);
        }
        return null;
    }
}