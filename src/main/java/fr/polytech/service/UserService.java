package fr.polytech.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.polytech.model.KeycloakLoginResponse;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Keycloak instance
     */
    private final Keycloak keycloak = Keycloak.getInstance(
            System.getenv("KEYCLOAK_URI"),
            System.getenv("KEYCLOAK_REALM"),
            System.getenv("ADMIN_USERNAME"),
            System.getenv("ADMIN_PASSWORD"),
            System.getenv("CLIENT_ID")
    );

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
     * Logout user
     * @param userId User id
     * @return String containing the response from the API
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public String logoutUser(String userId) throws HttpClientErrorException {
        UserResource userResource = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().get(userId);
        userResource.logout();
        return "User logged out";
    }

    /**
     * Register user
     * @param user JSON content
     * @return UserRepresentation containing the response from the API
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public UserRepresentation registerUser(UserRepresentation user) throws HttpClientErrorException {
        logger.info("Starting the registration process");
        if (user.getUsername() == null && user.getEmail() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Username and email must be provided");
        } else if (user.getUsername() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Username must be provided");
        } else if (user.getEmail() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Email must be provided");
        } else {
            keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().create(user);
            return keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().search(user.getUsername()).get(0);
        }
    }

    /**
     * Get all users
     * @return List of all users
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public List<UserRepresentation> getUsers() throws HttpClientErrorException {
        logger.info("Getting all users");
        List<UserRepresentation> users = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().list();
        users.removeIf(user -> user.getUsername().equals(System.getenv("ADMIN_USERNAME")));
        return users;
    }

    /**
     * Get user by id
     * @param id User id
     * @return User with the specified id
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public UserRepresentation getUserById(String id) throws HttpClientErrorException {
        UserResource userResource = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().get(id);
        UserRepresentation userRepresentation = userResource.toRepresentation();

        if (userRepresentation != null) {
            return userRepresentation;
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    /**
     * Update user
     * @param id User id
     * @param updatedUser User to update
     * @return Updated user
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public UserRepresentation updateUser(String id, UserRepresentation updatedUser) {
        logger.info("Updating user with id " + id);
        UserResource userResource = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().get(id);
        userResource.update(updatedUser);
        return keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().get(id).toRepresentation();
    }

    /**
     * Check if the "sub" field of the access token matches the user id
     * @param id    String - User id
     * @param token String - Access token
     * @return boolean - True if the "sub" field of the access token matches the user id, false otherwise
     */
    public boolean checkUser(String id, String token) throws HttpClientErrorException {
        String actualToken = token.split("Bearer ")[1];
        DecodedJWT jwt = JWT.decode(actualToken);
        String userId = jwt.getClaim("sub").asString();
        return userId.equals(id);
    }
}
