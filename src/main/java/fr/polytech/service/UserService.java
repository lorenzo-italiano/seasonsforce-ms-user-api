package fr.polytech.service;

import fr.polytech.model.KeycloakLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Login user
     * @param formParams Form parameters (x-www-form-urlencoded)
     * @return KeycloakLoginResponse containing the response from the API
     * @throws HttpClientErrorException if the API returns an error
     */
    public KeycloakLoginResponse loginUser(MultiValueMap<String, String> formParams) throws HttpClientErrorException {
        logger.info("Starting the login process");
        String url = System.getenv("LOGIN_URI");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);

        try {
            ResponseEntity<KeycloakLoginResponse> response = restTemplate.postForEntity(url, request, KeycloakLoginResponse.class);
            logger.info("User login completed");
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error while logging in user: " + e.getMessage());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }

    /**
     * Register user
     * @param jsonContent JSON content
     * @return String containing the response from the API
     * @throws HttpClientErrorException if the API returns an error
     * @throws Exception if the admin access token cannot be retrieved
     */
    public String registerUser(String jsonContent) throws Exception {
        logger.info("Starting the registration process");
        String adminAccessToken = getAdminAccessToken();
        if (adminAccessToken == null) {
            logger.error("Failed to get admin access token");
            throw new Exception("Failed to get admin access token");
        }
        String response = registerWithAccessToken(jsonContent, adminAccessToken);
        logger.info("User registration completed");
        return response;
    }

    /**
     * Get the admin access token
     * @return String containing the admin access token
     * @throws HttpClientErrorException if the API returns an error
     */
    private String getAdminAccessToken() throws HttpClientErrorException {
        String adminTokenUri = System.getenv("ADMIN_TOKEN_URI");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "admin-cli");
        map.add("username", System.getenv("ADMIN_USERNAME"));
        map.add("password", System.getenv("ADMIN_PASSWORD"));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<KeycloakLoginResponse> response;
        try {
            response = restTemplate.postForEntity(adminTokenUri, request, KeycloakLoginResponse.class);
            return response.getBody().getAccess_token();
        } catch (HttpClientErrorException e) {
            logger.error("Error while getting admin access token: " + e.getMessage());
            throw new HttpClientErrorException(e.getStatusCode());
        } catch (NullPointerException e) {
            logger.error("Error while getting admin access token: " + e.getMessage());
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Register user with access token
     * @param jsonContent JSON content
     * @param accessToken Access token
     * @return String containing the response from the API
     * @throws HttpClientErrorException if the API returns an error
     */
    private String registerWithAccessToken(String jsonContent, String accessToken) throws HttpClientErrorException {
        String registerUri = System.getenv("REGISTER_URI");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>(jsonContent, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(registerUri, request, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error while registering user: " + e.getMessage());
            throw new HttpClientErrorException(e.getStatusCode());
        }
    }
}
