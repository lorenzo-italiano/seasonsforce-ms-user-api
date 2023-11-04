package fr.polytech.service;

import fr.polytech.Util.Utils;
import fr.polytech.model.ReferenceDTO;
import fr.polytech.model.UpdateBody;
import fr.polytech.model.user.BaseUserResponse;
import fr.polytech.model.user.CandidateUserResponse;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static fr.polytech.constant.Env.REFERENCE_API_URI;
import static fr.polytech.constant.Roles.CANDIDATE;

@Service
public class ReferenceService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private final String referenceApiUri = System.getenv(REFERENCE_API_URI);

    @Autowired
    private UserService userService;

    /**
     * Add a reference to a candidate.
     *
     * @param id        User id of the candidate who receives the reference
     * @param reference Reference to add
     * @param token     String - Access token from the user who sent the reference
     * @return BaseUserResponse containing the response from the API
     */
    public BaseUserResponse addReference(String id, ReferenceDTO reference, String token) {
        logger.info("Adding reference to user with ID " + id);

        validateReference(id, reference, token);

        UserResource userResource = userService.getKeycloakUserResource(id);
        BaseUserResponse userResponse = Utils.userRepresentationToUserResponse(userResource.toRepresentation());

        validateCandidateRole(userResponse);

        CandidateUserResponse userToUpdate = (CandidateUserResponse) userResponse;

        ReferenceDTO referenceResponse = sendReferenceRequest(reference, token);
        List<UUID> references = userToUpdate.getReferenceIdList();
        references.add(referenceResponse.getId());
        userToUpdate.setReferenceIdList(references);

        // Update user
        UpdateBody updateBody = new UpdateBody();
        updateBody.setReferenceIdList(references);
        return userService.updateUser(id, updateBody);
    }

    /**
     * Send a POST reference request to the reference API.
     *
     * @param reference Reference to send.
     * @param token     String - Access token from the user who sent the reference.
     * @return ReferenceDTO containing the response from the API.
     */
    private ReferenceDTO sendReferenceRequest(ReferenceDTO reference, String token) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<ReferenceDTO> request = new HttpEntity<>(reference, headers);
        ResponseEntity<ReferenceDTO> response = restTemplate.postForEntity(referenceApiUri, request, ReferenceDTO.class);

        if (response.getBody() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid reference");
        }

        return response.getBody();
    }

    /**
     * Validate a reference before creating it.
     *
     * @param id        User id of the candidate who receives the reference.
     * @param reference Reference to validate.
     * @param token     String - Access token from the user who sent the reference.
     * @throws HttpClientErrorException If the reference is invalid.
     */
    private void validateReference(String id, ReferenceDTO reference, String token) throws HttpClientErrorException {
        // Check that the user who sent the reference is NOT the one who receives it
        if (!userService.checkUser(id, token)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid reference: User validation failed.");
        }
        // Check that the user who sent the reference is the one in the "senderId" field
        if (!reference.getSenderId().equals(UUID.fromString(id))) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid reference: Sender ID mismatch.");
        }
    }

    /**
     * Validate a candidate before adding a reference to them.
     *
     * @param userResponse User to validate.
     * @throws HttpClientErrorException If the user is not a candidate.
     */
    private void validateCandidateRole(BaseUserResponse userResponse) throws HttpClientErrorException {
        if (!userResponse.getRole().equals(CANDIDATE)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid role: User is not a candidate.");
        }
    }
}
