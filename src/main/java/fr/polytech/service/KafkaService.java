package fr.polytech.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.polytech.model.ExperienceDTO;
import fr.polytech.model.aux.ExperienceDTOWithUserId;
import fr.polytech.model.response.user.BaseUserResponse;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

import static fr.polytech.constant.Env.*;

@Service
public class KafkaService {

    /**
     * Initialize logger
     */
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private final ExperienceService experienceService;
    private final ObjectMapper objectMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaService(ExperienceService experienceService, ObjectMapper objectMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.experienceService = experienceService;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    private final Keycloak keycloak = Keycloak.getInstance(
            System.getenv(KEYCLOAK_URI),
            System.getenv(KEYCLOAK_REALM),
            System.getenv(ADMIN_USERNAME),
            System.getenv(ADMIN_PASSWORD),
            System.getenv(CLIENT_ID)
    );

    @KafkaListener(topics = "experience-topic", groupId = "user")
    public void listenExperience(String message) {
        try {
            logger.info("Received message: {}", message);
            ExperienceDTOWithUserId experienceDTOWithUserId = messageToExperience(message);

            ExperienceDTO experienceDTO = new ExperienceDTO();

            experienceDTO.setStartDate(experienceDTOWithUserId.getStartDate());
            experienceDTO.setCompanyId(experienceDTOWithUserId.getCompanyId());
            experienceDTO.setEndDate(experienceDTOWithUserId.getEndDate());
            experienceDTO.setJobTitle(experienceDTOWithUserId.getJobTitle());
            experienceDTO.setJobCategoryId(experienceDTOWithUserId.getJobCategoryId());

            // get access token for user of id experienceDTOWithUserId.getUserId().toString()
            String token = keycloak.tokenManager().getAccessTokenString();

            logger.info("user token: " + token);

            UUID experienceUuid = experienceService.addExperienceForUserManager(experienceDTOWithUserId.getUserId().toString(), experienceDTO, token);

            experienceDTOWithUserId.setId(experienceUuid);

            sendExperience(experienceDTOWithUserId);
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing message", e);
        }
    }

    private ExperienceDTOWithUserId messageToExperience(String message) throws JsonProcessingException {
        ExperienceDTOWithUserId experienceDTOWithUserId = objectMapper.readValue(message, new TypeReference<ExperienceDTOWithUserId>() {
        });
        logger.info("experienceDTOWithUserId received: {}", experienceDTOWithUserId);
        return experienceDTOWithUserId;
    }

    /**
     * Send a message to the Kafka topic
     *
     * @param experience ExperienceDTO to send
     * @throws HttpClientErrorException If the notification cannot be serialized
     */
    public void sendExperience(ExperienceDTOWithUserId experience) throws HttpClientErrorException {
        try {
            String message = objectMapper.writeValueAsString(experience);
            logger.info("Producing message: {}", message);
            kafkaTemplate.send("experience-creation-topic", message);
        } catch (JsonProcessingException e) {
            throw new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while serializing notification");
        }
    }
}
