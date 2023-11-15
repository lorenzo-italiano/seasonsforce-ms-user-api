package fr.polytech.restcontroller;


import fr.polytech.annotation.IsCandidate;
import fr.polytech.model.ReferenceDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.service.ReferenceService;
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
@RequestMapping("/api/v1/user/reference")
public class ReferenceController {

    private final Logger logger = LoggerFactory.getLogger(ReferenceController.class);

    @Autowired
    private final ReferenceService referenceService;

    public ReferenceController(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    /**
     * Add a reference to a candidate with the specified id
     *
     * @param id        User id
     * @param reference Reference to add
     * @param token     Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/{id}")
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
    @PatchMapping("/remove/{id}")
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
}
