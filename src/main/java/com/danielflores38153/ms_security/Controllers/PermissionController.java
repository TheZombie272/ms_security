package com.danielflores38153.ms_security.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import com.danielflores38153.ms_security.Models.Permission;
import com.danielflores38153.ms_security.Repositories.PermissionRepository;

@CrossOrigin
@RestController
@RequestMapping("/permission")
public class PermissionController {

     @Autowired
    private PermissionRepository thePermissionRepository;


    @GetMapping("")
    public List<Permission> getAllPermission() {
        return this.thePermissionRepository.findAll();

    }

    @GetMapping("/{id}")
    public Permission getPermissionById(@PathVariable String id) {
        return this.thePermissionRepository.findById(id).orElse(null);
    }

    @PostMapping("") //Se crea el permiso solo si el url y method no son null
    public List<Permission> createPermission(@RequestBody List<Permission> thePermissions) {
        List<Permission> RePermissions = new ArrayList<>();

        for ( Permission thePermission : thePermissions) {
            if (thePermission.getUrl() != null && thePermission.getMethod() != null) {
                RePermissions.add(this.thePermissionRepository.save(thePermission));
            } else{
                System.out.println(">>> Permission: No se creo el permiso"); 
            }
        }
        return RePermissions;
    }

    @DeleteMapping("{id}")
    public void deletPermission(@PathVariable String id) {
        Permission thePermission = this.thePermissionRepository.findById(id).orElse(null);

        if (thePermission != null) {
            this.thePermissionRepository.delete(thePermission);
        }else {
            System.out.println(">>> Permission: No se puede eliminar el permiso");
        }
    }

    @PutMapping("{id}")
    public Permission updateUser(@PathVariable String id, @RequestBody Permission updatePermission) {
        Permission thePermission = this.thePermissionRepository.findById(id).orElse(null);

        if (thePermission != null && updatePermission.getUrl() != null && updatePermission.getMethod() != null) {
            thePermission.setUrl(updatePermission.getUrl());
            thePermission.setMethod(updatePermission.getMethod());
            return this.thePermissionRepository.save(thePermission);
        }
        System.out.println(">>> Permission: no se edito correctamente el permiso");
        return null;
    }
}
