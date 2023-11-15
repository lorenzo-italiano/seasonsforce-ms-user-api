package fr.polytech.restcontroller;

import fr.polytech.annotation.*;
import fr.polytech.model.AvailabilityDTO;
import fr.polytech.model.ExperienceDTO;
import fr.polytech.model.ReferenceDTO;
import fr.polytech.model.ReviewDTO;
import fr.polytech.model.request.LoginDTO;
import fr.polytech.model.request.RefreshTokenDTO;
import fr.polytech.model.request.RegisterDTO;
import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.KeycloakLoginDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.service.*;
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


@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private final UserService userService;

    @Autowired
    private final ReferenceService referenceService;

    @Autowired
    private final ExperienceService experienceService;

    @Autowired
    private final AvailabilityService availabilityService;

    @Autowired
    private final ReviewService reviewService;

    /**
     * Constructor for UserController
     *
     * @param userService         UserService to inject
     * @param referenceService    ReferenceService to inject
     * @param experienceService   ExperienceService to inject
     * @param availabilityService AvailabilityService to inject
     * @param reviewService       ReviewService to inject
     */
    @Autowired
    public UserController(
            UserService userService,
            ReferenceService referenceService,
            ExperienceService experienceService,
            AvailabilityService availabilityService,
            ReviewService reviewService
    ) {
        this.userService = userService;
        this.referenceService = referenceService;
        this.experienceService = experienceService;
        this.availabilityService = availabilityService;
        this.reviewService = reviewService;
    }

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
    public ResponseEntity<Boolean> askToBeRemoved(@PathVariable("id") String id) {
        try {
            userService.askToBeRemoved(id);
            logger.info("User ask to be removed completed");
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while asking to be removed: " + e.getStatusCode());
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

    /**
     * Add a reference to a candidate with the specified id
     *
     * @param id        User id
     * @param reference Reference to add
     * @param token     Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/reference/{id}")
    @IsCandidate
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> addReference(
            @PathVariable("id") String id,
            @RequestBody ReferenceDTO reference,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = referenceService.addReference(id, reference, token);
            logger.info("Added reference to user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while adding reference to user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Remove a reference from a candidate with the specified id
     *
     * @param id        User id
     * @param reference Reference to remove
     * @param token     Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/remove/reference/{id}")
    @IsCandidate
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> removeReference(
            @PathVariable("id") String id,
            @RequestBody ReferenceDTO reference,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = referenceService.deleteReference(id, reference, token);
            logger.info("Removed reference from user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while removing reference from user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Add an experience to a candidate with the specified id
     *
     * @param id         User id
     * @param experience Experience to add
     * @param token      Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/experience/{id}")
    @IsCandidate
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> addExperience(
            @PathVariable("id") String id,
            @RequestBody ExperienceDTO experience,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = experienceService.addExperience(id, experience, token);
            logger.info("Added experience to user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while adding experience to user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Remove an experience from a candidate with the specified id
     *
     * @param id         User id
     * @param experience Experience to remove
     * @param token      Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/remove/experience/{id}")
    @IsCandidate
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> removeExperience(
            @PathVariable("id") String id,
            @RequestBody ExperienceDTO experience,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = experienceService.deleteExperience(id, experience, token);
            logger.info("Removed experience from user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while removing experience from user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Add an availability to a candidate with the specified id
     *
     * @param id           User id
     * @param availability Availability to add
     * @param token        Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/availability/{id}")
    @IsCandidate
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> addAvailability(
            @PathVariable("id") String id,
            @RequestBody AvailabilityDTO availability,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = availabilityService.addAvailability(id, availability, token);
            logger.info("Added availability to user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while adding availability to user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Remove an availability from a candidate with the specified id
     *
     * @param id           User id
     * @param availability Availability to remove
     * @param token        Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/remove/availability/{id}")
    @IsCandidate
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> removeAvailability(
            @PathVariable("id") String id,
            @RequestBody AvailabilityDTO availability,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = availabilityService.deleteAvailability(id, availability, token);
            logger.info("Removed availability from user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while removing availability from user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Add a review to a candidate with the specified id
     *
     * @param id     User id
     * @param review Availability to add
     * @param token  Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/review/{id}")
    @IsRecruiterOrAdmin
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> addReview(
            @PathVariable("id") String id,
            @RequestBody ReviewDTO review,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = reviewService.addReview(id, review, token);
            logger.info("Added review to user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while adding review to user: " + e.getStatusCode(), e);
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }

    /**
     * Remove a review from a candidate with the specified id
     *
     * @param id     User id
     * @param review Availability to remove
     * @param token  Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/remove/review/{id}")
    @IsRecruiterOrAdmin
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @Produces(MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BaseUserResponse> removeReview(
            @PathVariable("id") String id,
            @RequestBody ReviewDTO review,
            @RequestHeader("Authorization") String token
    ) {
        try {
            BaseUserResponse response = reviewService.deleteReview(id, review, token);
            logger.info("Removed review from user");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            logger.error("Error while removing review from user: " + e.getStatusCode());
            return new ResponseEntity<>(null, e.getStatusCode());
        }
    }
}
