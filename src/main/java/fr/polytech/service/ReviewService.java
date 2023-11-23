package fr.polytech.service;

import fr.polytech.Util.Utils;
import fr.polytech.model.ExperienceDTO;
import fr.polytech.model.ReviewDTO;
import fr.polytech.model.aux.OfferDTO;
import fr.polytech.model.aux.OfferDetailDTO;
import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.model.response.user.CandidateUserResponse;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
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

    @Autowired
    private ApiService apiService;

    @Autowired
    private ExperienceService experienceService;

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

        // Change Offer status to "REVIEWED"

//        apiService.makeApiCall(System.getenv("OFFER_API_URI") + "/reviewed/" + review.getOfferId(), HttpMethod.PATCH, OfferDTO.class, token, null);

        logger.info("token to send {}", token);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.setBearerAuth(token.split(" ")[1]);

        // Création d'une entité HTTP avec en-têtes
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        // Envoi de la requête PATCH avec un corps null
        ResponseEntity<String> responseEntity = restTemplate.exchange(System.getenv("OFFER_API_URI") + "/reviewed/" + review.getOfferId(), HttpMethod.PATCH, requestEntity, String.class);

        // Add experience to user
//        OfferDTO offerApiUri = apiService.makeApiCall(System.getenv("OFFER_API_URI") + "/" + review.getOfferId(), HttpMethod.GET, OfferDTO.class, token, null);

//        logger.info("Successfully fetched offerDTO");

//        ExperienceDTO experienceDTO = new ExperienceDTO();
//        experienceDTO.setCompanyId(offerApiUri.getCompanyId());
//        experienceDTO.setStartDate(offerApiUri.getStartDate());
//        experienceDTO.setEndDate(offerApiUri.getEndDate());
//        experienceDTO.setJobCategoryId(offerApiUri.getJobCategoryId());
//        experienceDTO.setJobTitle(offerApiUri.getJob_title());

//        experienceService.addExperience(id, experienceDTO, token);

        // Update user
        UpdateDTO updateDTO = new UpdateDTO();
        updateDTO.setReviewIdList(reviews);
        // Don't update other fields
        updateDTO.setReferenceIdList(userToUpdate.getReferenceIdList());
        updateDTO.setExperienceIdList(userToUpdate.getExperienceIdList());
        updateDTO.setAvailabilityIdList(userToUpdate.getAvailabilityIdList());
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
        // Don't update other fields
        updateDTO.setReferenceIdList(userToUpdate.getReferenceIdList());
        updateDTO.setExperienceIdList(userToUpdate.getExperienceIdList());
        updateDTO.setAvailabilityIdList(userToUpdate.getAvailabilityIdList());

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
