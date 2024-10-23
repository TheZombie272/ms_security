package com.danielflores38153.ms_security.Controllers;

import com.danielflores38153.ms_security.Models.Role;
import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Models.UserRole;
import com.danielflores38153.ms_security.Repositories.UserRepository;
import com.danielflores38153.ms_security.Repositories.UserRoleRepository;
import com.danielflores38153.ms_security.Services.EncryptionService;
import com.danielflores38153.ms_security.Services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private UserRoleRepository theUserRoleRepository; // Repositorio para roles de usuario
    @Autowired
    private EncryptionService theEncryptionService;
    @Autowired
    private JwtService theJwtService;

    // Mapa para contar los logins por rol
    private final Map<String, Integer> roleLoginCounter = new HashMap<>();

    @PostMapping("/login")
    public HashMap<String, Object> login(@RequestBody User theNewUser,
                                         final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        String token = "";
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());
        System.out.println(theActualUser);

        // Verificación de credenciales
        if (theActualUser != null &&
                theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {
            token = theJwtService.generateToken(theActualUser);
            theActualUser.setPassword(""); // Limpiar la contraseña
            theResponse.put("token", token);
            theResponse.put("user", theActualUser);

            // Obtener los roles del usuario
            List<UserRole> userRoles = getUserRolesByUserId(theActualUser.get_id());

            // Actualizar el contador de logins por cada rol del usuario
            userRoles.forEach(userRole -> {
                String roleName = userRole.getRole().getName(); // Obtener el nombre del rol
                roleLoginCounter.put(roleName, roleLoginCounter.getOrDefault(roleName, 0) + 1);
            });

            // Imprimir el mapa para verificar el conteo de roles
            System.out.println("Contador de logins por rol: " + roleLoginCounter);

            // Obtener el rol con más logins
            String mostLoggedRole = getMostLoggedRole();
            theResponse.put("mostLoggedRole", mostLoggedRole);

            return theResponse;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return theResponse;
        }
    }

    // Método para obtener los roles del usuario por su ID
    public List<UserRole> getUserRolesByUserId(@PathVariable String userId) {
        return this.theUserRoleRepository.getUserRolesByUserId(userId);
    }

    // Método para obtener el rol con más logins
    private String getMostLoggedRole() {
        return roleLoginCounter.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
