package fr.polytech.service;

import fr.polytech.Util.Utils;
import fr.polytech.model.ExperienceDTO;
import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.model.response.user.CandidateUserResponse;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static fr.polytech.constant.Env.EXPERIENCE_API_URI;

@Service
public class ExperienceService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    @Qualifier("loadBalancedRestTemplate")
    private RestTemplate loadBalancedRestTemplate;

    private final String experienceApiUri = System.getenv(EXPERIENCE_API_URI);

    @Autowired
    private UserService userService;

    /**
     * Add an experience to a candidate.
     *
     * @param id         User id of the candidate who adds the experience
     * @param experience Experience to add
     * @param token      String - Access token from the user who adds the experience
     * @return BaseUserResponse containing the response from the API
     */
    public BaseUserResponse addExperience(String id, ExperienceDTO experience, String token) {
        logger.info("Adding experience to user with ID " + id);

        UserResource userResource = userService.getKeycloakUserResource(id);
        BaseUserResponse userResponse = Utils.userRepresentationToUserResponse(userResource.toRepresentation());
        CandidateUserResponse userToUpdate = (CandidateUserResponse) userResponse;

        ExperienceDTO experienceResponse = createExperienceRequest(experience, token);
        List<UUID> experiences = userToUpdate.getExperienceIdList();
        experiences.add(experienceResponse.getId());

        // Update user
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setExperienceIdList(experiences);
        return userService.updateUser(id, updateDTO);
    }

    /**
     * Remove an experience from a candidate.
     *
     * @param id         User id of the candidate who removes the experience
     * @param experience Experience to remove.
     * @param token      String - Access token from the user who ask to remove the experience.
     * @return BaseUserResponse containing the response from the API.
     * @throws HttpClientErrorException If the user is not authorized.
     */
    public BaseUserResponse deleteExperience(String id, ExperienceDTO experience, String token) throws HttpClientErrorException {
        logger.info("Removing experience from user with ID " + id);

        boolean checkedUser = userService.checkUser(id, token);
        if (!checkedUser) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "User not authorized");
        }

        UserResource userResource = userService.getKeycloakUserResource(id);
        BaseUserResponse userResponse = Utils.userRepresentationToUserResponse(userResource.toRepresentation());
        CandidateUserResponse userToUpdate = (CandidateUserResponse) userResponse;

        List<UUID> experiences = userToUpdate.getExperienceIdList();
        experiences.remove(experience.getId());

        // Update user
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setExperienceIdList(experiences);
        BaseUserResponse updatedUser = userService.updateUser(id, updateDTO);

        // Remove reference from reference API
        removeExperienceRequest(experience, token);
        return updatedUser;
    }

    /**
     * Send a POST experience request to the experience API.
     *
     * @param experience Experience to send.
     * @param token      String - Access token from the user who sent the experience.
     * @return ExperienceDTO containing the response from the API.
     * @throws HttpClientErrorException If the experience is invalid.
     */
    private ExperienceDTO createExperienceRequest(ExperienceDTO experience, String token) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<ExperienceDTO> request = new HttpEntity<>(experience, headers);
        ResponseEntity<ExperienceDTO> response = loadBalancedRestTemplate.postForEntity(experienceApiUri, request, ExperienceDTO.class);

        if (response.getBody() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid experience");
        }

        return response.getBody();
    }

    /**
     * Send a DELETE experience request to the experience API.
     *
     * @param experience Experience to remove.
     * @param token      String - Access token from the user who sent the experience.
     * @throws HttpClientErrorException If the experience is invalid.
     */
    private void removeExperienceRequest(ExperienceDTO experience, String token) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<ExperienceDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = loadBalancedRestTemplate.exchange(experienceApiUri + "/" + experience.getId(), HttpMethod.DELETE, request, Boolean.class);

        if (response.getBody() == null || !response.getBody()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Invalid experience");
        }
    }
}
