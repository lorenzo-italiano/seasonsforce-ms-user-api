package fr.polytech.service;

import fr.polytech.Util.Utils;
import fr.polytech.model.AvailabilityDTO;
import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.model.response.user.CandidateUserResponse;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static fr.polytech.constant.Env.AVAILABILITY_API_URI;

@Service
public class AvailabilityService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private RestTemplate restTemplate;

    private final String availabilityApiUri = System.getenv(AVAILABILITY_API_URI);

    @Autowired
    private UserService userService;

    /**
     * Add an availability to a candidate.
     *
     * @param id           User id of the candidate who adds the availability
     * @param availability Availability to add
     * @param token        String - Access token from the user who adds the availability
     * @return BaseUserResponse containing the response from the API
     */
    public BaseUserResponse addAvailability(String id, AvailabilityDTO availability, String token) {
        logger.info("Adding availability to user with ID " + id);

        validateUser(id, token);

        UserResource userResource = userService.getKeycloakUserResource(id);
        BaseUserResponse userResponse = Utils.userRepresentationToUserResponse(userResource.toRepresentation());
        CandidateUserResponse userToUpdate = (CandidateUserResponse) userResponse;

        AvailabilityDTO availabilityResponse = createAvailabilityRequest(availability, token);

        List<UUID> availabilities = userToUpdate.getAvailabilityIdList();
        if (availabilities == null) {
            availabilities = new ArrayList<>();
        }
        availabilities.add(availabilityResponse.getId());

        // Update user
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setAvailabilityIdList(availabilities);
        // Don't update the other fields
        updateDTO.setReferenceIdList(userToUpdate.getReferenceIdList());
        updateDTO.setExperienceIdList(userToUpdate.getExperienceIdList());
        updateDTO.setReviewIdList(userToUpdate.getReviewIdList());
        return userService.updateUser(id, updateDTO);
    }

    /**
     * Remove an availability from a candidate.
     *
     * @param id           User id of the candidate who removes the availability
     * @param availability Availability to remove.
     * @param token        String - Access token from the user who ask to remove the availability.
     * @return BaseUserResponse containing the response from the API.
     * @throws HttpClientErrorException If the user is not authorized.
     */
    public BaseUserResponse deleteAvailability(String id, AvailabilityDTO availability, String token) throws HttpClientErrorException {
        logger.info("Removing availability from user with ID " + id);

        validateUser(id, token);

        UserResource userResource = userService.getKeycloakUserResource(id);
        BaseUserResponse userResponse = Utils.userRepresentationToUserResponse(userResource.toRepresentation());
        CandidateUserResponse userToUpdate = (CandidateUserResponse) userResponse;

        logger.info("deleteAvailability: userToUpdate = " + userToUpdate);
        List<UUID> availabilities = userToUpdate.getAvailabilityIdList();
        logger.info("deleteAvailability: userToUpdate.availabilities = " + availabilities);
        if (availabilities == null || !availabilities.contains(availability.getId())) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Availability not found");
        }
        availabilities.remove(availability.getId());
        logger.info("deleteAvailability: userToUpdate.availabilities.remove(availability.getId()) = " + availabilities);
        // Update user
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setAvailabilityIdList(availabilities);
        // Don't update the other fields
        updateDTO.setReferenceIdList(userToUpdate.getReferenceIdList());
        updateDTO.setExperienceIdList(userToUpdate.getExperienceIdList());
        updateDTO.setReviewIdList(userToUpdate.getReviewIdList());

        BaseUserResponse updatedUser = userService.updateUser(id, updateDTO);

        // Remove availability from availability API
        removeAvailabilityRequest(availability, token);
        return updatedUser;
    }

    /**
     * Send a POST availability request to the availability API.
     *
     * @param availability Availability to send.
     * @param token        String - Access token from the user who sent the availability.
     * @return AvailabilityDTO containing the response from the API.
     * @throws HttpClientErrorException If the availability is invalid.
     */
    private AvailabilityDTO createAvailabilityRequest(AvailabilityDTO availability, String token) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.split(" ")[1]);

        HttpEntity<AvailabilityDTO> request = new HttpEntity<>(availability, headers);
        ResponseEntity<AvailabilityDTO> response = restTemplate.postForEntity(availabilityApiUri + "/", request, AvailabilityDTO.class);

        if (response.getBody() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid availability");
        }

        return response.getBody();
    }

    /**
     * Send a DELETE availability request to the availability API.
     *
     * @param availability Availability to remove.
     * @param token        String - Access token from the user who sent the availability.
     * @throws HttpClientErrorException If the availability is invalid.
     */
    private void removeAvailabilityRequest(AvailabilityDTO availability, String token) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.split(" ")[1]);

        HttpEntity<AvailabilityDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(availabilityApiUri + "/" + availability.getId(), HttpMethod.DELETE, request, Boolean.class);

        if (response.getBody() == null || !response.getBody()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Invalid availability");
        }
    }

    /**
     * Check if the user is valid.
     *
     * @param id    User id of the candidate who receives the availability.
     * @param token String - Access token from the user who sent the availability.
     * @throws HttpClientErrorException If the availability is invalid.
     */
    private void validateUser(String id, String token) throws HttpClientErrorException {
        if (!userService.checkUser(id, token)) {
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, "User not authorized");
        }
    }
}
