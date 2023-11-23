package fr.polytech.restcontroller;

import fr.polytech.annotation.*;
import fr.polytech.model.request.LoginDTO;
import fr.polytech.model.request.RefreshTokenDTO;
import fr.polytech.model.request.RegisterDTO;
import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.KeycloakLoginDTO;
import fr.polytech.model.response.SearchedRecruiter;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.model.response.user.detailed.DetailedBaseUserResponse;
import fr.polytech.model.response.user.plan.PlanUser;
import fr.polytech.service.MatchingService;
import fr.polytech.service.UserService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MatchingService matchingService;

    /**
     * Login endpoint
     *
     * @param loginDTO JSON content
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/auth/login")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeycloakLoginDTO> login(@RequestBody LoginDTO loginDTO) {
        try {
            KeycloakLoginDTO response = userService.loginUser(loginDTO.getUsername(), loginDTO.getPassword());
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
     *
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
     *
     * @param registerDTO JSON content
     * @return ResponseEntity containing the response from the API
     */
    @PostMapping("/auth/register")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeycloakLoginDTO> register(@RequestBody RegisterDTO registerDTO) {
        try {
            logger.info("Starting the registration process");
            KeycloakLoginDTO response = userService.registerUser(registerDTO);
            logger.info("User registration completed");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (HttpClientErrorException e) {
            logger.error("Error while registering user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Refresh token endpoint
     *
     * @param requestBody JSON content
     */
    @PostMapping("/auth/refresh")
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<KeycloakLoginDTO> refreshToken(@RequestBody RefreshTokenDTO requestBody) {
        try {
            KeycloakLoginDTO newTokens = userService.refreshToken(requestBody.getRefresh_token());
            return new ResponseEntity<>(newTokens, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Ask to be removed endpoint
     *
     * @param id User id
     */
    @PostMapping("/delete-me/{id}")
    @IsCandidateOrRecruiterAndSender
    @Produces(MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Boolean> askToBeRemoved(@PathVariable("id") String id, @RequestHeader("Authorization") String token) {
        try {
            userService.askToBeRemoved(id);
            logger.info("User ask to be removed completed");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while asking to be removed: " + e.getStatusCode(), e);
            return new ResponseEntity<>(false, e.getStatusCode());
        }
    }

    /**
     * Delete user endpoint
     *
     * @param id User id
     */
    @DeleteMapping("/{id}")
    @IsAdmin
    @Produces(MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<Boolean> deleteUser(@PathVariable("id") String id) {
        try {
            userService.deleteUser(id);
            logger.info("User delete completed");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while deleting user: " + e.getStatusCode());
            return new ResponseEntity<>(false, e.getStatusCode());
        }
    }

    /**
     * Get all users
     *
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/")
    @IsAdmin
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BaseUserResponse>> getAllUsers() {
        try {
            List<BaseUserResponse> response = userService.getUsers();
            logger.info("Users get completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while getting all users: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Match user availabilities with a job offer.
     * The quantity of candidates and their attributes depends on the plan of the recruiter.
     *
     * @param offerId Job offer id
     * @param token   Access token
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/match/{offerId}")
    @IsRecruiter
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PlanUser>> matchUsersWithOffer(@PathVariable("offerId") UUID offerId, @RequestHeader("Authorization") String token) {
        try {
            List<PlanUser> response = matchingService.matchUsersWithOffer(offerId, token);
            logger.info("Users match completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while matching users: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Search recruiters by first name and/or last name
     *
     * @param firstName First name
     * @param lastName  Last name
     * @param token     Access token
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/search")
    @IsCandidate
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SearchedRecruiter>> searchUsers(
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestHeader("Authorization") String token
    ) {
        try {
            List<SearchedRecruiter> response = userService.searchUsers(firstName, lastName, token);
            logger.info("Users search completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while searching users: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Get user by id
     *
     * @param id User id
     * @return ResponseEntity containing the response from the API
     */
    @GetMapping("/{id}")
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> getUserById(@PathVariable("id") String id) {
        try {
            BaseUserResponse response = userService.getUserById(id);
            logger.info("User get completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while getting user " + id + ". Error: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    @GetMapping("/detailed/{id}")
    @IsSender
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DetailedBaseUserResponse> getDetailedUserById(@RequestHeader("Authorization") String token, @PathVariable("id") String id) {
        try {
            DetailedBaseUserResponse response = userService.getDetailedUserById(id, token);
            logger.info("User get completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while getting user " + id + ". Error: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    @GetMapping("/to-be-removed")
    @IsAdmin
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BaseUserResponse>> getUsersToBeRemoved() {
        try {
            List<BaseUserResponse> response = userService.getUsersToBeRemoved();
            logger.info("Users get completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while getting all users: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Update user
     *
     * @param id    User id
     * @param user  User to update
     * @param token Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/{id}")
    @IsSender
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> updateUser(
            @PathVariable("id") String id,
            @RequestBody UpdateDTO user,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = userService.updateUser(id, user);
            logger.info("User update completed");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while updating user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }
}
