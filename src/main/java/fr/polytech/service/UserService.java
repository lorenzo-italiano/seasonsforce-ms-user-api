package fr.polytech.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.polytech.Util.Utils;
import fr.polytech.model.request.RegisterDTO;
import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.KeycloakLoginDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static fr.polytech.constant.Env.*;
import static fr.polytech.constant.Roles.CANDIDATE;
import static fr.polytech.constant.Roles.RECRUITER;


@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Keycloak instance
     */
    private final Keycloak keycloak = Keycloak.getInstance(
            System.getenv(KEYCLOAK_URI),
            System.getenv(KEYCLOAK_REALM),
            System.getenv(ADMIN_USERNAME),
            System.getenv(ADMIN_PASSWORD),
            System.getenv(CLIENT_ID)
    );

    /**
     * Login user
     *
     * @param username String - Username
     * @param password String - Password
     * @return KeycloakLoginResponse containing the response from the API
     * @throws HttpClientErrorException if the API returns an error
     */
    public KeycloakLoginDTO loginUser(String username, String password) throws HttpClientErrorException {
        logger.info("Starting the login process");
        String url = System.getenv("LOGIN_URI");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formParams = new HttpHeaders();
        formParams.add("grant_type", "password");
        formParams.add("client_id", System.getenv("CLIENT_ID"));
        formParams.add("username", username);
        formParams.add("password", password);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);

        ResponseEntity<KeycloakLoginDTO> response = restTemplate.postForEntity(url, request, KeycloakLoginDTO.class);
        return response.getBody();
    }

    /**
     * Logout user
     *
     * @param userId User id
     * @return String containing the response from the API
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public String logoutUser(String userId) throws HttpClientErrorException {
        logger.info("Starting the logout process");
        UserResource userResource = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().get(userId);
        userResource.logout();
        return "User logged out";
    }

    /**
     * Refresh access token
     *
     * @param refreshToken Refresh token
     * @return KeycloakLoginResponse containing the response from the API
     */
    public KeycloakLoginDTO refreshToken(String refreshToken) throws HttpClientErrorException {
        logger.info("Refreshing access token");
        String url = System.getenv("LOGIN_URI");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formParams = new HttpHeaders();
        formParams.add("grant_type", "refresh_token");
        formParams.add("client_id", System.getenv("CLIENT_ID"));
        formParams.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formParams, headers);

        ResponseEntity<KeycloakLoginDTO> response = restTemplate.postForEntity(url, request, KeycloakLoginDTO.class);
        return response.getBody();
    }

    /**
     * Register user
     *
     * @param user JSON content
     * @return UserRepresentation containing the response from the API
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public KeycloakLoginDTO registerUser(RegisterDTO user) throws HttpClientErrorException {
        logger.info("Starting the registration process");

        // Check if all fields are present
        if (user.getEmail() == null || user.getPassword() == null || user.getFirstName() == null ||
                user.getLastName() == null || user.getRole() == null || user.getGender() == null ||
                user.getBirthdate() == null || user.getCitizenship() == null || user.getPhone() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing fields : " + user);
        }

        if (!user.getRole().equals(CANDIDATE) && !user.getRole().equals(RECRUITER)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid role");
        }

        // Build username from email
        String username = user.getEmail().split("@")[0];

        // Set credentials
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(user.getPassword());
        credentialRepresentation.setTemporary(false);

        // Create user representation
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);

        // Set user representation
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setEmailVerified(false);
        userRepresentation.setUsername(username);
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        userRepresentation.setGroups(List.of("User/" +
                user.getRole().substring(0, 1).toUpperCase() +
                user.getRole().substring(1).toLowerCase()));

        // Add specific attributes
        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("birthdate", Collections.singletonList(user.getBirthdate().toString()));
        attributes.put("citizenship", Collections.singletonList(user.getCitizenship()));
        attributes.put("gender", Collections.singletonList(user.getGender().toString()));
        attributes.put("phone", Collections.singletonList(user.getPhone()));
        attributes.put("role", Collections.singletonList(user.getRole().toLowerCase()));
        attributes.put("isRegistered", Collections.singletonList("false"));

        userRepresentation.setAttributes(attributes);

        // Create user and return it
        Response response = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().create(userRepresentation);
        logger.info("User created with status " + response.getStatus());
        if (response.getStatus() == 201) {
            return this.loginUser(username, user.getPassword());
        } else {
            throw new HttpClientErrorException(Optional.ofNullable(HttpStatus.resolve(response.getStatus())).orElse(HttpStatus.INTERNAL_SERVER_ERROR), "User already exists");
        }
    }

    /**
     * Get all users
     *
     * @return List of all users
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public List<BaseUserResponse> getUsers() throws HttpClientErrorException {
        logger.info("Getting all users");
        List<UserRepresentation> users = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().list();
        users.removeIf(user -> user.getUsername().equals(System.getenv("ADMIN_USERNAME")));
        List<BaseUserResponse> userResponses = new ArrayList<>();
        for (UserRepresentation user : users) {
            userResponses.add(Utils.userRepresentationToUserResponse(user));
        }
        return userResponses;
    }

    /**
     * Get user by id
     *
     * @param id User id
     * @return User with the specified id
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public BaseUserResponse getUserById(String id) throws HttpClientErrorException {
        UserResource userResource = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().get(id);
        UserRepresentation userRepresentation = userResource.toRepresentation();

        if (userRepresentation != null) {
            return Utils.userRepresentationToUserResponse(userRepresentation);
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    /**
     * Update user without overriding all attributes
     *
     * @param id          User id
     * @param updatedUser User to update
     * @return Updated user
     * @throws HttpClientErrorException if the API returns an error or if the admin access token cannot be retrieved
     */
    public BaseUserResponse updateUser(String id, UpdateDTO updatedUser) throws HttpClientErrorException {
        logger.info("Updating user with ID " + id);
        UserResource userResource = getKeycloakUserResource(id);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        Map<String, List<String>> previousAttributes = userRepresentation.getAttributes();

        if (previousAttributes == null) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "User not found");
        }

        Utils.validateAttributes(userRepresentation, previousAttributes.get("role").get(0));

        UserRepresentation updateUserRepresentation = Utils.updateBodyToUserRepresentation(updatedUser, previousAttributes.get("role").get(0));
        Map<String, List<String>> updatedAttributes = this.getAttributesToUpdate(updateUserRepresentation, previousAttributes);

        // Update the UserRepresentation with the merged attributes
        userRepresentation.setAttributes(updatedAttributes);

        if (updatedUser.getUsername() != null) {
            List<UserRepresentation> existingUsers = keycloak.realm(System.getenv("KEYCLOAK_REALM")).users().search(updatedUser.getUsername());
            if (!existingUsers.isEmpty()) {
                throw new HttpClientErrorException(HttpStatus.CONFLICT, "Username already exists");
            } else {
                userRepresentation.setUsername(updatedUser.getUsername());
            }
        } else if (updatedUser.getFirstName() != null) {
            userRepresentation.setFirstName(updatedUser.getFirstName());
        } else if (updatedUser.getLastName() != null) {
            userRepresentation.setLastName(updatedUser.getLastName());
        }

        // Call Keycloak to update the user
        userResource.update(userRepresentation);
        return Utils.userRepresentationToUserResponse(getKeycloakUserResource(id).toRepresentation());
    }

    /**
     * Get attributes to update
     *
     * @param updateUserRepresentation UserRepresentation - User to update
     * @param previousAttributes       Map<String, List<String>> - Previous attributes
     * @return Map<String, List < String>> - Attributes to update
     */
    private Map<String, List<String>> getAttributesToUpdate(UserRepresentation updateUserRepresentation, Map<String, List<String>> previousAttributes) {
        Map<String, List<String>> newAttributes = updateUserRepresentation.getAttributes();
        Map<String, List<String>> attributesToUpdate = new HashMap<>();

        // First, put all previous attributes into the map
        previousAttributes.forEach((key, value) -> attributesToUpdate.put(key, new ArrayList<>(value)));

        newAttributes.forEach((key, newValue) -> {
            if (attributesToUpdate.containsKey(key)) {
                if (!newValue.equals(Collections.singletonList(null))) {
                    attributesToUpdate.put(key, newValue);
                }
            } else {
                attributesToUpdate.put(key, newValue);
            }
        });

        return attributesToUpdate;
    }

    /**
     * Get User Resource from Keycloak
     *
     * @param id User id
     * @return UserResource from Keycloak
     */
    UserResource getKeycloakUserResource(String id) {
        return keycloak.realm(System.getenv(KEYCLOAK_REALM)).users().get(id);
    }

    /**
     * Check if the "sub" field of the access token matches the user id
     *
     * @param id    String - User id
     * @param token String - Access token
     * @return boolean - True if the "sub" field of the access token matches the user id, false otherwise
     * @throws HttpClientErrorException if an error occurs while decoding the token
     */
    public boolean checkUser(String id, String token) throws HttpClientErrorException {
        String actualToken = token.split("Bearer ")[1];
        DecodedJWT jwt = JWT.decode(actualToken);
        String userId = jwt.getClaim("sub").asString();
        return userId.equals(id);
    }
}
