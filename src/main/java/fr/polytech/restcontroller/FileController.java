package fr.polytech.restcontroller;

import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.service.MinioService;
import fr.polytech.service.UserService;
import io.minio.errors.MinioException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1/user/profile-picture")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private final UserService userService;

    @Autowired
    private final MinioService minioService;

    /**
     * Constructor.
     *
     * @param userService  UserService
     * @param minioService MinioService
     */
    public FileController(UserService userService, MinioService minioService) {
        this.userService = userService;
        this.minioService = minioService;
    }

    /**
     * Add a profile picture to a user.
     *
     * @param id   User id
     * @param file Profile picture file
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/profile-picture/add/{id}")
    @PreAuthorize("hasAnyRole('client_candidate', 'client_recruiter')")
    @Consumes(MediaType.MULTIPART_FORM_DATA_VALUE)
    @Produces(MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<BaseUserResponse> addProfilePicture(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Upload the file to MinIO
            minioService.uploadFile(id, id, file, true);

            // Update the user's profile picture URL
            UpdateDTO updateDTO = new UpdateDTO();
            updateDTO.setProfilePictureUrl(System.getenv("MINIO_PUBLIC_URI") + "/" + id + "/" + id);
            BaseUserResponse updatedUser = userService.updateUser(id, updateDTO);

            logger.info("Added profile picture to user");
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(null, e.getStatusCode());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (MinioException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Remove a profile picture from a user.
     *
     * @param id User id
     * @return ResponseEntity containing the response from the API
     */
    @PatchMapping("/profile-picture/remove/{id}")
    @PreAuthorize("hasAnyRole('client_candidate', 'client_recruiter') || @userService.checkUser(#id, #token)")
    @Produces(MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<BaseUserResponse> removeProfilePicture(
            @PathVariable("id") String id,
            @RequestHeader("Authorization") String token
    ) {
        try {
            // Delete the file from MinIO
            minioService.deleteFileFromBucket(id, id);

            // Update the user's profile picture URL
            UpdateDTO updateDTO = new UpdateDTO();
            updateDTO.setProfilePictureUrl(null);
            BaseUserResponse updatedUser = userService.updateUser(id, updateDTO);

            logger.info("Removed profile picture from user");
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (HttpClientErrorException e) {
            return new ResponseEntity<>(null, e.getStatusCode());
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (MinioException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
