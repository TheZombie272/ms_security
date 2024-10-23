/*
package com.danielflores38153.ms_security.Controllers;

import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Models.UserRole;
import com.danielflores38153.ms_security.Models.Role;
import com.danielflores38153.ms_security.Models.Session;
import com.danielflores38153.ms_security.Repositories.UserRoleRepository;
import com.danielflores38153.ms_security.Repositories.RoleRepository;
import com.danielflores38153.ms_security.Repositories.SessionRepository;
import com.danielflores38153.ms_security.Repositories.UserRepository;
import com.danielflores38153.ms_security.Services.EncryptionService;
import com.danielflores38153.ms_security.Services.HttpService;
import com.danielflores38153.ms_security.Services.JwtService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Map;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private EncryptionService theEncryptionService;
    @Autowired
    private JwtService theJwtService;
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private HttpService theHttpService;

    // Sustentación
    @Autowired
    private UserRoleRepository theUserRoleRepository;
    @Autowired
    private RoleRepository theRoleRepository;

    @PostMapping("/login")
    public HashMap<String, Object> login(@RequestBody User theNewUser,
                                         final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        String token = "";
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());
        System.out.println(theActualUser);
        if (theActualUser != null && //Todo esto ocurre si primero el usuario existe y segundo si se inserta la contraseña correcta
                theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {
                
            String secureInt = String.valueOf((new SecureRandom()).nextInt(1000000));
            //this.send2FA(secureInt, theActualUser); // Enviamos el correo con el código para el 2FA

            Session theSession = new Session(token, theActualUser); //Iniciamos una session con un token '' vacio
            theSession.setToken2FA(secureInt);
            this.theSessionRepository.save(theSession); // Guardamos la session
            theSession.setToken2FA("");
            theSession.setUser(null);
            theResponse.put("Session", theSession); // Ponemos la session sin el usuario y sin el token2FA

            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return theResponse;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return theResponse;
        }

    }

    @GetMapping("/role_login")
    public Role role_login(){
        // Buscamos por todas las sessiones
        List<Session> sesiones = this.theSessionRepository.findAll();

        // Ordenamos todos los roles para poder contarlos
        List<Role> roles = this.theRoleRepository.findAll();
        
        HashMap<Role, Integer> conteo = new HashMap<>(); // HashMap para contar

        // Ponemos todos los roles en modo clave valor comenzando con 0
        for (Role role : roles) {
            conteo.put(role, 0);
        }

        for (Session session : sesiones) {
            List<UserRole> theUserRoles = this.theUserRoleRepository.getUserRolesByUserId(session.getUser().get_id());

            for (UserRole userRole : theUserRoles) {
                conteo.put(userRole.getRole(), conteo.get(userRole.getRole())+1);
            }
        }

        Role roleMax = null;
        int maxConteo = 0;
        
        for (Role role : conteo.keySet()) {  // Recorremos todas las llaves para obetener el mayor
            if (conteo.get(role) > maxConteo) {
                maxConteo = conteo.get(role); // Actualizar el máximo
                roleMax = role; // Actualizar la clave correspondiente
            }
        }

        System.out.println(conteo.toString());
        return roleMax;
    }


    @PostMapping("/2FA")
    public HashMap<String, Object> twoFactorAuth(
		@RequestBody Session theIncomingSession,
		final HttpServletResponse servletResponse
	) throws IOException {
        Session theActiveSession = this.theSessionRepository.findById(
			theIncomingSession.get_id()
		).orElse(null);

        HashMap<String, Object> theResponse = new HashMap<>();
        String token = "";

        if(theActiveSession != null) {
            if (
                theActiveSession.getToken2FA().equals(theIncomingSession.getToken2FA()) &&
                theActiveSession.getExpirationDateTime().isAfter(LocalDateTime.now()) &&
                !theActiveSession.isUsed()
            ) {
                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theActiveSession.getUser().getEmail()
                );

                token = this.theJwtService.generateToken(theCurrentUser);

                theActiveSession.setUsed(true); //Agregamos al token lo que falta
                theActiveSession.setToken(token);

                this.theSessionRepository.save(theActiveSession);
                theCurrentUser.setPassword("");
                theResponse.put("token", token);
                theResponse.put("user", theCurrentUser);
                return theResponse;
            } else {

                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return null;

    }


    public String send2FA(String code, User user){
        /*
         * {
    "reciepient":[
        {
            "name": "a",
            "email": "a@gmail.com"
        },
        {
            "name": "b",
            "email" : "b@gmail.com"
        }
    ],
    "subject": "this it the subject",
    "content": "this is the content",
    "template": "2-fa"
}
*/

package com.danielflores38153.ms_security.Controllers;
import java.io.IOException;
import java.util.HashMap;

import com.danielflores38153.ms_security.Models.Session;
import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Repositories.SessionRepository;
import com.danielflores38153.ms_security.Repositories.UserRepository;
import com.danielflores38153.ms_security.Services.EmailService;
import com.danielflores38153.ms_security.Services.EncryptionService;
import com.danielflores38153.ms_security.Services.JwtService;
import com.danielflores38153.ms_security.Services.PasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin
@RestController
@RequestMapping("/security")
public class SecurityController {

    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private EncryptionService theEncryptionService;
    @Autowired
    private JwtService theJwtService;
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private EmailService theEmailService;

    @PostMapping("/login")
    public HashMap<String, Object> login(@RequestBody User theNewUser,
                                         final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());
        if (theActualUser != null
                && theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {
            String token2FA = PasswordService.generateToken();

            theActualUser.setPassword("");
            theResponse.put("user", theActualUser);
            theResponse.put("token2FA", token2FA);

            //crear sesion
            Session theUserSession = new Session();
            theUserSession.setUser(theActualUser); // Establece el usuario para la sesión
            theUserSession.setToken2FA(token2FA); // Guarda el token 2FA
            theSessionRepository.save(theUserSession); // Guarda la sesión en la base de datos

            // Enviar token 2FA por correo
            this.theEmailService.sendEmail(theActualUser.getEmail(), "Código",
                    "Sus datos: " + theUserSession.get_id() + " Token: " + token2FA);
            return theResponse;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return theResponse;
        }

    }

    // Verificar token y generar jwt
    @PostMapping("/verify2fa")
    public String verify2FA(@RequestBody Session theSession, final HttpServletResponse response) throws IOException {
        Session theUserSession = this.theSessionRepository.findById(theSession.get_id()).orElse(null);
        if (theUserSession != null && theUserSession.getToken2FA().equals(theSession.getToken2FA())) {
            // Generar token JWT
            String token = this.theJwtService.generateToken(theUserSession.getUser());

            // Actualizar la sesión
            theUserSession.setToken(token);
            theSessionRepository.save(theUserSession);

            return token;
        }  else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Código de verificación incorrecto");
            return null;
        }
    }

}
