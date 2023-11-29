package fr.polytech.restcontroller;


import fr.polytech.annotation.IsRecruiterOrAdmin;
import fr.polytech.model.ReviewDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.service.ReviewService;
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

@RestController
@RequestMapping("/api/v1/user/review")
public class ReviewController {

    private final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    @Autowired
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Add a review to a candidate with the specified id
     *
     * @param id     User id
     * @param review Availability to add
     * @param token  Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/{id}")
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
    @PatchMapping("/remove/{id}")
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
