package fr.polytech.restcontroller;


import fr.polytech.annotation.IsCandidate;
import fr.polytech.model.AvailabilityDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.service.AvailabilityService;
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
@RequestMapping("/api/v1/user/availability")
public class AvailabilityController {

    private final Logger logger = LoggerFactory.getLogger(AvailabilityController.class);

    @Autowired
    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Add an availability to a candidate with the specified id
     *
     * @param id           User id
     * @param availability Availability to add
     * @param token        Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/{id}")
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
    @PatchMapping("/remove/{id}")
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
}
