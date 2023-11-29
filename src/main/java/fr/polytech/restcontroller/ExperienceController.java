package fr.polytech.restcontroller;


import fr.polytech.annotation.IsCandidate;
import fr.polytech.model.ExperienceDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.service.ExperienceService;
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
@RequestMapping("/api/v1/user/experience")
public class ExperienceController {

    private final Logger logger = LoggerFactory.getLogger(ExperienceController.class);

    @Autowired
    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    /**
     * Add an experience to a candidate with the specified id
     *
     * @param id         User id
     * @param experience Experience to add
     * @param token      Access token
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/add/{id}")
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
    @PatchMapping("/remove/{id}")
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
}
