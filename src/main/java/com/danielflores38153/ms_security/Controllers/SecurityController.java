package com.danielflores38153.ms_security.Controllers;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private EmailService theEmailService;

    // Mapa para contar los logins por rol
    private final Map<String, Integer> roleLoginCounter = new HashMap<>();

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

            //crear sesion
            Session theUserSession = new Session();
            theUserSession.setUser(theActualUser); // Establece el usuario para la sesión
            theUserSession.setToken2FA(token2FA); // Guarda el token 2FA
            theUserSession.setType("session");
            Session theSendSession=theSessionRepository.save(theUserSession); // Guarda la sesión en la base de datos

            // Enviar token 2FA por correo
            this.theEmailService.sendEmail(theActualUser.getEmail(), "Código",
                    "Sus datos: " + theUserSession.get_id() + " Token: " + token2FA);
              System.out.println(token2FA);
            theSendSession.setToken2FA(null);
            theResponse.put("session", theSendSession);
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
        if (theUserSession != null && theUserSession.getToken2FA().equals(theSession.getToken2FA())
        && !theUserSession.getUsed() && theUserSession.getType().equals("session")) {
            // Generar token JWT
            String token = this.theJwtService.generateToken(theUserSession.getUser());

            // Actualizar la sesión
            theUserSession.setToken(token);
            theUserSession.setUsed(true);
            theSessionRepository.save(theUserSession);

            return token;
        }  else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Código de verificación incorrecto");
            return null;
        }
    }

    @PostMapping("/reset_password")
    public HashMap<String, Object> reset_password(@RequestBody User theNewUser,
                                         final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());
        if (theActualUser != null) {

            String token2FA = PasswordService.generateToken();

            theActualUser.setPassword("");

            //crear sesion
            Session theUserSession = new Session();
            theUserSession.setUser(theActualUser); // Establece el usuario para la sesión
            theUserSession.setToken2FA(token2FA); // Guarda el token 2FA
            theUserSession.setToken(theNewUser.getPassword()); // Pasamos la nueva password como el token xd
            theUserSession.setType("reset_password");
            Session theSendSession=theSessionRepository.save(theUserSession); // Guarda la sesión en la base de datos

            // Enviar token 2FA por correo
            this.theEmailService.sendEmail(theActualUser.getEmail(), "Código",
                    "Sus datos: " + theUserSession.get_id() + " Token: " + token2FA);
            theSendSession.setToken2FA(null);
            theSendSession.setToken(null);
            theResponse.put("session", theSendSession);
            return theResponse;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return theResponse;
        }

    }

    @PostMapping("/new_password")
    public String new_password(@RequestBody Session theSession, final HttpServletResponse response) throws IOException {
        Session theUserSession = this.theSessionRepository.findById(theSession.get_id()).orElse(null);
        if (theUserSession != null && theUserSession.getToken2FA().equals(theSession.getToken2FA())
        && !theUserSession.getUsed() && theUserSession.getType().equals("reset_password")) {
            
            User theUser = this.theUserRepository.findById(theUserSession.getUser().get_id()).orElse(null);

            theUser.setPassword(this.theEncryptionService.convertSHA256(theUserSession.getToken())); // Cambiamos la contraseña por la que habia sido guardada en el token xd
            this.theUserRepository.save(theUser); // Guardamos el usuario

            // Actualizar la sesión
            theUserSession.setUsed(true);
            theUserSession.setToken(null);
            theSessionRepository.save(theUserSession);

            return "Contraseña cambiada";
        }  else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session incorrecta");
            return null;
        }
    }
}
