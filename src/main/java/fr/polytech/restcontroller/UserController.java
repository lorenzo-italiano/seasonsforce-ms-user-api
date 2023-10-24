package fr.polytech.restcontroller;

import fr.polytech.model.KeycloakLoginResponse;
import fr.polytech.model.User;
import fr.polytech.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;


@RestController
@RequestMapping("/api/v1/user")
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
     * @param formParams Form parameters
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/auth/login")
    public ResponseEntity<KeycloakLoginResponse> login(@RequestBody MultiValueMap<String, String> formParams) {
        try {
            KeycloakLoginResponse response = userService.loginUser(formParams);
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
     * Register endpoint
     * @param jsonContent JSON content
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/auth/register")
    public ResponseEntity<String> register(@RequestBody String jsonContent) {
        try {
            String response = userService.registerUser(jsonContent);
            logger.info("User registration completed");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (HttpClientErrorException e) {
            logger.error("Error while registering user: " + e.getStatusCode());
            return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
        }
    }

    /**
     * Get all users
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('client_admin')")
    public ResponseEntity<List<Object>> getAllUsers() { // TODO: Change to List<User> when interface User is implemented
        try {
            List<Object> response = userService.getUsers(); // TODO: Change to List<User> when interface User is implemented
            logger.info("User registration completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while registering user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Get user by id
     * @param id User id
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable("id") String id) { // TODO: Change to User when interface User is implemented
        try {
            Object response = userService.getUserById(id); // TODO: Change to User when interface User is implemented
            logger.info("User registration completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while registering user: " + e.getStatusCode());
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
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateUser( // TODO: Change to List<User> when interface User is implemented
            @PathVariable("id") String id,
            @RequestBody Object user, // TODO: Change to User when interface User is implemented
            @RequestHeader("Authorization") String token
    ) {
        try {
            userService.checkUser(id, token);
            logger.info("User checked");
            Object response = userService.updateUser(id, user); // TODO: Change to User when interface User is implemented
            logger.info("User registration completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while registering user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }
}
