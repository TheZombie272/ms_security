package com.danielflores38153.ms_security.Controllers;

import com.danielflores38153.ms_security.Models.User;
import com.danielflores38153.ms_security.Models.Session;
import com.danielflores38153.ms_security.Repositories.SessionRepository;
import com.danielflores38153.ms_security.Repositories.UserRepository;
import com.danielflores38153.ms_security.Services.EncryptionService;
import com.danielflores38153.ms_security.Services.HttpService;
import com.danielflores38153.ms_security.Services.JwtService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
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
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private HttpService theHttpService;

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
            this.send2FA(secureInt, theActualUser); // Enviamos el correo con el código para el 2FA

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

        String body = "";
        body = body+"\"recipients\":[{\"name\":\""+user.getName()+"\", \"email\":\""+user.getEmail()+"\"}]";
        body = body+", \"subject\":\"2FA\""+", \"content\":\""+code+"\"";
        body = body+"\template\":\"2FA\"";

        this.theHttpService.postNotification("/", body);
        return null;

    }

}


/*
 * 
 * package edu.prog3.mssecurity.Controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONException;
import org.json.JSONObject;

import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Models.Permission;
import edu.prog3.mssecurity.Models.Session;
import edu.prog3.mssecurity.Models.ErrorStatistic;
import edu.prog3.mssecurity.Repositories.ErrorStatisticRepository;
import edu.prog3.mssecurity.Repositories.SessionRepository;
import edu.prog3.mssecurity.Repositories.UserRepository;
import edu.prog3.mssecurity.Services.SecurityService;
import edu.prog3.mssecurity.Services.HttpService;
import edu.prog3.mssecurity.Services.JwtService;
import edu.prog3.mssecurity.Services.ValidatorsService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin    
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private SecurityService theSecurityService;
    @Autowired
    private JwtService theJwtService;
    @Autowired
    private HttpService theHttpService;
    @Autowired
    private ValidatorsService theValidatorsService;
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private ErrorStatisticRepository theErrorStatisticRepository;

	
    @PostMapping("login")
    public ResponseEntity<String> login(
        @RequestBody User theUser,
        final HttpServletResponse servletResponse
    ) throws IOException {
        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());
        ResponseEntity<String> securityResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (theCurrentUser != null) {
            if (theCurrentUser.getPassword().equals(
                this.theSecurityService.convertSHA256(theUser.getPassword())
            )) {
                int code = new Random().nextInt(1000000);
                Session theSession = new Session(String.valueOf(code), theCurrentUser);
                this.theSessionRepository.save(theSession);
    
                JSONObject body = new JSONObject();
                body.put("to", theUser.getEmail());
                body.put("template", "TWOFACTOR");
                body.put("pin", code);

                ResponseEntity<String> notificationResponse = this.theHttpService.postNotification("/send_email", body);
                JSONObject json = new JSONObject(
					notificationResponse.getBody()).put("session_id", theSession.get_id()
				);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
                securityResponse = new ResponseEntity<String>(
					json.toString(),
					headers,
					notificationResponse.getStatusCode()
				);
            } else {
                ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                    	.getErrorStatisticByUser(theCurrentUser.get_id());
                
                if (theErrorStatistic != null) {
                    theErrorStatistic.setNumAuthErrors(
                        theErrorStatistic.getNumAuthErrors() + 1
                    );
                } else {
					theErrorStatistic = new ErrorStatistic(0, 1, theCurrentUser);
				}
                this.theErrorStatisticRepository.save(theErrorStatistic);

                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return securityResponse;
    }

    @PostMapping("2FA")
    public ResponseEntity<String> twoFactorAuth(
		@RequestBody Session theIncomingSession,
		final HttpServletResponse servletResponse
	) throws IOException {
        Session theActiveSession = this.theSessionRepository.findBy_id(
			new ObjectId(theIncomingSession.get_id())
		);
        String token = "";

        if(theActiveSession != null) {
            if (
                theActiveSession.getCode().equals(theIncomingSession.getCode()) &&
                theActiveSession.getExpirationDateTime().isAfter(LocalDateTime.now()) &&
                !theActiveSession.isUsed()
            ) {
                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theActiveSession.getUser().getEmail()
                );

                token = this.theJwtService.generateToken(theCurrentUser);
                theActiveSession.setUsed(true);
                this.theSessionRepository.save(theActiveSession);
            } else {
                ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                    	.getErrorStatisticByUser(theActiveSession.getUser().get_id());
                
                if (theErrorStatistic != null) {
                    theErrorStatistic.setNumAuthErrors(
                        theErrorStatistic.getNumAuthErrors() + 1
                    );
                } else {
					theErrorStatistic = new ErrorStatistic(0, 1, theActiveSession.getUser());
				}
                this.theErrorStatisticRepository.save(theErrorStatistic);

                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> response = new ResponseEntity<>(
			new JSONObject().put("token", token).toString(), headers, HttpStatus.OK
		);
        return response;
    }

    @PostMapping("pw-reset")
    public ResponseEntity<String> passwordReset(
		@RequestBody User theUser,
		final HttpServletResponse response
	) throws IOException, URISyntaxException {
        String message = "Si el correo ingresado está asociado a una cuenta, " +
		"pronto recibirá un mensaje para restablecer su contraseña.";
		ResponseEntity<String> securityResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);;

        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());

        if (theCurrentUser != null) {
            String code = this.theSecurityService.getRandomAlphanumerical(6);

            Session theSession = new Session(code, theCurrentUser);
            this.theSessionRepository.save(theSession);

            JSONObject body = new JSONObject();
            body.put("to", theUser.getEmail());
            body.put("template", "PWRESET");
            body.put("url", code);
			
            ResponseEntity<String> notificationResponse = this.theHttpService.postNotification("/send_email", body);
			JSONObject json = new JSONObject(notificationResponse.getBody())
					.put("session_id", theSession.get_id())
					.put("message", message);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			securityResponse = new ResponseEntity<String>(
				json.toString(),
				headers,
				notificationResponse.getStatusCode()
			);
        }

        return securityResponse;
    }


    public ErrorStatistic getHighestSecurityErrors() {
        List<ErrorStatistic> theErrorStatistics = this.theErrorStatisticRepository.findAll();
        ErrorStatistic highest = new ErrorStatistic();
        for (ErrorStatistic es : theErrorStatistics) {
            if (es.getNumSecurityErrors() > highest.getNumSecurityErrors()) {
                highest = es;
            }
        }
        return highest;
    }

	@PostMapping("validate-permissions")
    public ResponseEntity<String> validatePermissions(
        final HttpServletRequest request,
        @RequestBody Permission thePermission
    ) {
        boolean success = this.theValidatorsService.validateRolePermission(
            request,
            thePermission.getUrl(),
            thePermission.getMethod()
        );

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> response = new ResponseEntity<>(
			new JSONObject().put("success", success).toString(), headers, HttpStatus.OK
		);
        return response;
    }

	@PostMapping("validator")
    public String validator(
        @RequestBody Map<String, Object> body,
        final HttpServletResponse response
    ) throws IOException {
        String message = "Rare type";
        System.out.println(body.toString()+ "ssssssssssssssssss");
        try {
            if (!body.containsKey("type")) {
                return "Invalid request: missing 'type' field";
            }

            String type = (String)body.get("type");

            switch (type) {
                case "two-factor":
					// FIXME - Broken
                    //message = this.twoFactorAuth(body, response);
                    break;
                case "reset-password":
                    message = this.resetPassword(body, response);
                    break;
                default:
                    return "Invalid request: unknown type '" + type + "'";
            }
        } catch (JSONException e) {
            return "Invalid request: " + e.getMessage();
        }

        return message;
    }

    public String resetPassword(Map<String, Object> body, HttpServletResponse response) throws IOException{
        Session theCurrentSession = this.theSessionRepository.findBy_id(
			new ObjectId((String)body.get("id"))
		);

        String message = "";
        System.out.println(theCurrentSession);

        if(theCurrentSession != null) {
            if (
                theCurrentSession.getCode().equals((String)body.get("code")) &&
                theCurrentSession.getExpirationDateTime().isAfter(LocalDateTime.now()) &&
                !theCurrentSession.isUsed()
            ) {

                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theCurrentSession.getUser().getEmail()
                );

                theCurrentUser.setPassword(theSecurityService.convertSHA256((String)body.get("password")));
                this.theUserRepository.save(theCurrentUser);

                theCurrentSession.setUsed(true);
                this.theSessionRepository.save(theCurrentSession);
                message = "the password was update";
            } else {
                ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                    	.getErrorStatisticByUser(theCurrentSession.getUser().get_id());
                
                if (theErrorStatistic != null) {
                    theErrorStatistic.setNumAuthErrors(
                        theErrorStatistic.getNumAuthErrors() + 1
                    );
                } else {
					theErrorStatistic = new ErrorStatistic(0, 1, theCurrentSession.getUser());
				}
                this.theErrorStatisticRepository.save(theErrorStatistic);

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

        return message;
    }
}
 */