package fr.polytech.restcontroller;

import fr.polytech.model.KeycloakLoginResponse;
import fr.polytech.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;


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
}
