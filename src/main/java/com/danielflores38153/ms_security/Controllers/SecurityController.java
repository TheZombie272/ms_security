/*
package com.danielflores38153.ms_security.Controllers;

import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Repositories.UserRepository;
import com.danielflores38153.ms_security.Services.EncryptionService;
import com.danielflores38153.ms_security.Services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;

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

    @PostMapping("/login")
    public HashMap<String, Object> login(@RequestBody User theNewUser,
                                         final HttpServletResponse response) throws IOException {
        HashMap<String, Object> theResponse = new HashMap<>();
        String token = "";
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());
        System.out.println(theActualUser);
        if (theActualUser != null &&
                theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {
            token = theJwtService.generateToken(theActualUser);
            theActualUser.setPassword("");
            theResponse.put("token", token);
            theResponse.put("user", theActualUser);
            return theResponse;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return theResponse;
        }

    }

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