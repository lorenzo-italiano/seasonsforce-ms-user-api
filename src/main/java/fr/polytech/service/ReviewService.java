package fr.polytech.service;

import fr.polytech.Util.Utils;
import fr.polytech.model.ReviewDTO;
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

import static fr.polytech.constant.Env.REVIEW_API_URI;

@Service
public class ReviewService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private RestTemplate restTemplate;

    private final String reviewApiUri = System.getenv(REVIEW_API_URI);

    @Autowired
    private UserService userService;

    /**
     * Add a review to a candidate.
     *
     * @param id     User id of the candidate who adds the review
     * @param review Review to add
     * @param token  String - Access token from the user who adds the review
     * @return BaseUserResponse containing the response from the API
     */
    public BaseUserResponse addReview(String id, ReviewDTO review, String token) {
        logger.info("Adding review to user with ID " + id);

        UserResource userResource = userService.getKeycloakUserResource(id);
        BaseUserResponse userResponse = Utils.userRepresentationToUserResponse(userResource.toRepresentation());
        CandidateUserResponse userToUpdate = (CandidateUserResponse) userResponse;
        ReviewDTO reviewResponse = createReviewRequest(review, token);
        List<UUID> reviews = userToUpdate.getReviewIdList();

        if (reviews == null) {
            reviews = new ArrayList<>();
        }

        reviews.add(reviewResponse.getId());

        // Update user
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setReviewIdList(reviews);
        return userService.updateUser(id, updateDTO);
    }

    /**
     * Remove a review from a candidate.
     *
     * @param id     User id of the candidate who removes the review
     * @param review Review to remove.
     * @param token  String - Access token from the user who ask to remove the review.
     * @return BaseUserResponse containing the response from the API.
     * @throws HttpClientErrorException If the user is not authorized.
     */
    public BaseUserResponse deleteReview(String id, ReviewDTO review, String token) throws HttpClientErrorException {
        logger.info("Removing review from user with ID " + id);

        UserResource userResource = userService.getKeycloakUserResource(id);
        BaseUserResponse userResponse = Utils.userRepresentationToUserResponse(userResource.toRepresentation());
        CandidateUserResponse userToUpdate = (CandidateUserResponse) userResponse;

        List<UUID> reviews = userToUpdate.getReviewIdList();
        if (reviews == null || !reviews.contains(review.getId())) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Review not found");
        }
        reviews.remove(review.getId());

        // Update user
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setReviewIdList(reviews);
        BaseUserResponse updatedUser = userService.updateUser(id, updateDTO);

        // Remove review from review API
        removeReviewRequest(review, token);
        return updatedUser;
    }

    /**
     * Send a POST review request to the review API.
     *
     * @param review Review to send.
     * @param token  String - Access token from the user who sent the review.
     * @return ReviewDTO containing the response from the API.
     * @throws HttpClientErrorException If the review is invalid.
     */
    private ReviewDTO createReviewRequest(ReviewDTO review, String token) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.split(" ")[1]);

        HttpEntity<ReviewDTO> request = new HttpEntity<>(review, headers);
        ResponseEntity<ReviewDTO> response = restTemplate.postForEntity(reviewApiUri + "/", request, ReviewDTO.class);

        if (response.getBody() == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid review");
        }

        return response.getBody();
    }

    /**
     * Send a DELETE review request to the review API.
     *
     * @param review Review to remove.
     * @param token  String - Access token from the user who sent the review.
     * @throws HttpClientErrorException If the review is invalid.
     */
    private void removeReviewRequest(ReviewDTO review, String token) throws HttpClientErrorException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token.split(" ")[1]);

        HttpEntity<ReviewDTO> request = new HttpEntity<>(headers);
        ResponseEntity<Boolean> response = restTemplate.exchange(reviewApiUri + "/" + review.getId(), HttpMethod.DELETE, request, Boolean.class);

        if (response.getBody() == null || !response.getBody()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Invalid review");
        }
    }
}
