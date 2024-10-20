package com.danielflores38153.ms_security.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.danielflores38153.ms_security.Models.Role;
import com.danielflores38153.ms_security.Models.Permission;
import com.danielflores38153.ms_security.Models.RolePermission;
import com.danielflores38153.ms_security.Repositories.RoleRepository;
import com.danielflores38153.ms_security.Repositories.PermissionRepository;
import com.danielflores38153.ms_security.Repositories.RolePermissionRepository;

@Controller
@RestController
@RequestMapping("/api/role_permissions")
public class RolePermissionController {
    
    @Autowired
    private PermissionRepository thePermissionRepository;
    @Autowired
    private RolePermissionRepository theRolePermissionRepository;
    @Autowired
    private RoleRepository roleRepository;


    @GetMapping("")
    public List<RolePermission> getAllRolePermission() {
        return this.theRolePermissionRepository.findAll();

    }

    @GetMapping("/{id}")
    public RolePermission getRolePermissionById(@PathVariable String id) {
        return this.theRolePermissionRepository.findById(id).orElse(null);
    }

    @GetMapping("/role/{roleId}")
    public List<RolePermission> getRolePermissionsByRoleId(@PathVariable String roleId) {
        return this.theRolePermissionRepository.getPermissionsByRole(roleId);
    }

    @PostMapping("permission/{permissionId}/role/{roleId}")
    public RolePermission createRolePermission( @PathVariable String permissionId, @PathVariable String roleId) {
        Permission thePermission = this.thePermissionRepository.findById(permissionId).orElse(null);
        Role theRole = this.roleRepository.findById(roleId).orElse(null);

        if (thePermission != null && theRole != null) {
            RolePermission theRolePermission = new RolePermission();
            theRolePermission.setPermission(thePermission);
            theRolePermission.setRole(theRole);

            return theRolePermissionRepository.save(theRolePermission);
        }
        return null;
    }

    @DeleteMapping("{id}")
    public void deleteRolePermission(@PathVariable String id) {
        RolePermission theRolePermission = this.theRolePermissionRepository.findById(id).orElse(null);

        if (theRolePermission != null) {
            this.theRolePermissionRepository.delete(theRolePermission);
        }else {
            System.out.println("No se puede eliminar el role_permission");
        }
    }

    @PutMapping("{id}")
    public RolePermission updateRolePermission(@PathVariable String id, @RequestBody RolePermission updateRolePermission) {
        RolePermission theRolePermission = this.theRolePermissionRepository.findById(id).orElse(null);
        Permission thePermission = this.thePermissionRepository.findById(updateRolePermission.getPermission().get_id()).orElse(null);
        Role theRole = this.roleRepository.findById(updateRolePermission.getRole().get_id()).orElse(null);

        if (theRolePermission != null && theRole != null && thePermission != null) {
            theRolePermission.setPermission(thePermission);
            theRolePermission.setRole(theRole);
            return this.theRolePermissionRepository.save(theRolePermission);
        }
        return null;
    }
}
