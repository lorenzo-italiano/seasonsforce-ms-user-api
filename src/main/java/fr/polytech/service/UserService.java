package fr.polytech.service;

import fr.polytech.model.KeycloakLoginResponse;
import fr.polytech.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.LinkedHashMap;
import java.util.List;


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

        ResponseEntity<KeycloakLoginResponse> response = restTemplate.postForEntity(url, request, KeycloakLoginResponse.class);
        return response.getBody();
    }

    /**
     * Register user
     * @param jsonContent JSON content
     * @return String containing the response from the API
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public String registerUser(String jsonContent) throws HttpClientErrorException {
        logger.info("Starting the registration process");
        String adminAccessToken = getAdminAccessToken();
        return registerWithAccessToken(jsonContent, adminAccessToken);
    }

    /**
     * Get all users
     * @return List of all users
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public List<Object> getUsers() throws HttpClientErrorException { // TODO: Change to List<User> when interface User is implemented
        logger.info("Getting all users");
        String url = System.getenv("USER_URI");
        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
        List<Object> users = response.getBody(); // TODO: Change to List<User> when interface User is implemented
        users.removeIf(user -> ((LinkedHashMap) user).get("username").equals(System.getenv("ADMIN_USERNAME")));
        return users;
    }

    /**
     * Get user by id
     * @param id User id
     * @return User with the specified id
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public Object getUserById(String id) throws HttpClientErrorException { // TODO: Change to User when interface User is implemented
        logger.info("Getting user with id " + id);
        String url = System.getenv("USER_URI") + "/" + id;
        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(adminToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.GET, request, Object.class); // TODO: Change to User when interface User is implemented
        return response.getBody();
    }

    /**
     * Update user
     * @param id User id
     * @param user User to update
     * @return Updated user
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public Object updateUser(String id, Object user) { // TODO: Change to User when interface User is implemented
        logger.info("Updating user with id " + id);
        String url = System.getenv("USER_URI") + "/" + id;
        String adminToken = getAdminAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(adminToken);
        HttpEntity<Object> request = new HttpEntity<>(user, headers); // TODO: Change to User when interface User is implemented

        ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.PUT, request, Object.class); // TODO: Change to User when interface User is implemented
        return response.getBody();
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
        response = restTemplate.postForEntity(adminTokenUri, request, KeycloakLoginResponse.class);
        if (response.getBody() == null) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while retrieving the admin access token");
        }
        return response.getBody().getAccess_token();
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

        ResponseEntity<String> response = restTemplate.postForEntity(registerUri, request, String.class);
        return response.getBody();
    }

    /**
     * Check if the "sub" field of the access token matches the user id
     * @param id String - User id
     * @param token String - Access token
     */
    public void checkUser(String id, String token) throws HttpClientErrorException {
        String actualToken = token.split("Bearer ")[1];
        DecodedJWT jwt = JWT.decode(actualToken);
        String userId = jwt.getClaim("sub").asString();

        if (!userId.equals(id)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "The user id does not match the access token");
        }
    }
}
