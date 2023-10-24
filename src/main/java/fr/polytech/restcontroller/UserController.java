package fr.polytech.restcontroller;

import fr.polytech.model.KeycloakLoginResponse;
import fr.polytech.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    private final RestTemplate restTemplate = new RestTemplate();

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
        String url = System.getenv("LOGIN_URI");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);

        return restTemplate.exchange(url, HttpMethod.POST, request, KeycloakLoginResponse.class);
    }

    /**
     * Register endpoint
     * @param jsonContent JSON content
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/auth/register")
    public ResponseEntity<String> register(@RequestBody String jsonContent) {
        String adminTokenUri = System.getenv("ADMIN_TOKEN_URI");
        String registerUri = System.getenv("REGISTER_URI");

        // Get admin token
        // Post request with x-www-form-urlencoded content
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "admin-cli");
        map.add("username", System.getenv("ADMIN_USERNAME"));
        map.add("password", System.getenv("ADMIN_PASSWORD"));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(adminTokenUri, request, Map.class);

        // Get access token from response
        String accessToken;
        try { // The get method can throw a NullPointerException
            accessToken = (String) response.getBody().get("access_token");
        } catch (NullPointerException e) {
            return new ResponseEntity<>("An error occurred on the authentication server", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Register user
        // Post request with access token in Authorization header and jsonContent in body
        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);
        registerHeaders.setBearerAuth(accessToken);

        HttpEntity<String> registerRequest = new HttpEntity<>(jsonContent, registerHeaders);

        try {
            ResponseEntity<String> registerResponse = restTemplate.postForEntity(registerUri, registerRequest, String.class);
            return new ResponseEntity<>(registerResponse.getBody(), registerResponse.getStatusCode());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return new ResponseEntity<>("User exists with the same email", HttpStatus.CONFLICT);
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return new ResponseEntity<>("Invalid request", HttpStatus.BAD_REQUEST);
            }
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
            }
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return new ResponseEntity<>("Forbidden", HttpStatus.FORBIDDEN);
            }
            if (e.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return new ResponseEntity<>("An error occurred on the authentication server", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
        }
    }
}
