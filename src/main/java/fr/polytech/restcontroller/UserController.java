package fr.polytech.restcontroller;

import fr.polytech.model.KeycloakLoginResponse;
import fr.polytech.model.LoginBody;
import fr.polytech.model.RefreshTokenBody;
import fr.polytech.service.UserService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;


@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin("*") // TODO: Add our urls to the allowed origins
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private final UserService userService;

    /**
     * Constructor for UserController
     * @param userService UserService to inject
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Login endpoint
     * @param loginBody JSON content
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeycloakLoginResponse> login(@RequestBody LoginBody loginBody) {
        try {
            KeycloakLoginResponse response = userService.loginUser(loginBody.getUsername(), loginBody.getPassword());
            logger.info("User login completed");
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException e) {
            logger.error("Error while logging in user: " + e.getStatusCode());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.badRequest().build();
            } else {
                return ResponseEntity.internalServerError().build();
            }
        }
    }

    /**
     * Logout endpoint
     * @param id User id
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/logout/{id}")
    @Produces(MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> logout(@PathVariable("id") String id) {
        try {
            String response = userService.logoutUser(id);
            logger.info("User logout completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while logging out user: " + e.getStatusCode());
            return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
        }
    }

    /**
     * Register endpoint
     * @param user JSON content
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/auth/register")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRepresentation> register(@RequestBody UserRepresentation user) {
        try {
            UserRepresentation response = userService.registerUser(user);
            logger.info("User registration completed");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (HttpClientErrorException e) {
            logger.error("Error while registering user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Refresh token endpoint
     * @param requestBody JSON content
     */
    @PostMapping("/auth/refresh")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeycloakLoginResponse> refreshToken(@RequestBody RefreshTokenBody requestBody) {
        try {
            KeycloakLoginResponse newTokens = userService.refreshToken(requestBody.getRefresh_token());
            return new ResponseEntity<>(newTokens, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get all users
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('client_admin')")
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserRepresentation>> getAllUsers() {
        try {
            List<UserRepresentation> response = userService.getUsers();
            logger.info("Users get completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while getting all users: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Get user by id
     * @param id User id
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/{id}")
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRepresentation> getUserById(@PathVariable("id") String id) {
        try {
            UserRepresentation response = userService.getUserById(id);
            logger.info("User get completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while getting user " + id + ". Error: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Update user
     * @param id User id
     * @param user User to update
     * @param token Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/{id}")
    @PreAuthorize("@userService.checkUser(#id, #token)")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserRepresentation> updateUser(
            @PathVariable("id") String id,
            @RequestBody UserRepresentation user,
            @RequestHeader("Authorization") String token
    ) {
        try {
            UserRepresentation response = userService.updateUser(id, user);
            logger.info("User update completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while updating user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }
}
