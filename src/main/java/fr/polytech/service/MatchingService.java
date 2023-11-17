package fr.polytech.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import fr.polytech.model.AvailabilityDTO;
import fr.polytech.model.aux.OfferDetailDTO;
import fr.polytech.model.aux.PlanDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.model.response.user.CandidateUserResponse;
import fr.polytech.model.response.user.RecruiterUserResponse;
import fr.polytech.model.response.user.plan.FreeUser;
import fr.polytech.model.response.user.plan.PlanUser;
import fr.polytech.model.response.user.plan.SilverGoldPlatinumUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static fr.polytech.constant.Env.*;
import static fr.polytech.constant.Plans.*;
import static fr.polytech.constant.Roles.CANDIDATE;
import static fr.polytech.constant.Roles.RECRUITER;

@Service
public class MatchingService {

    /**
     * Initialize logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MatchingService.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserService userService;

    private static final String availabilityApiUri = System.getenv(AVAILABILITY_API_URI);
    private static final String offerApiUri = System.getenv(OFFER_API_URI);
    private static final String planApiUri = System.getenv(PLAN_API_URI);

    /**
     * Match users with an offer
     *
     * @param offerId UUID of the offer to match with
     * @param token   String - Access token from the user who adds the review
     * @return List of PlanUser containing users matching with the offer
     * @throws HttpClientErrorException if an error occurs while calling the API
     */
    public List<PlanUser> matchUsersWithOffer(UUID offerId, String token) throws HttpClientErrorException {
        logger.info("Getting all users matching with offer with ID " + offerId);

        // Get all users
        List<BaseUserResponse> users = userService.getUsers();

        // Get all availabilities of all users
        Map<CandidateUserResponse, List<AvailabilityDTO>> usersAvailabilities = new HashMap<>();

        users.stream().filter(user -> user.getRole().equals(CANDIDATE)).forEach(user -> {
            List<AvailabilityDTO> userAvailabilities = getAvailabilities((CandidateUserResponse) user, token);
            usersAvailabilities.put((CandidateUserResponse) user, userAvailabilities);
        });

        if (usersAvailabilities.isEmpty()) {
            return Collections.emptyList();
        }

        // Get offer and associated data
        OfferDetailDTO offer = makeApiCall(offerApiUri + "/" + offerId, HttpMethod.GET, OfferDetailDTO.class, token);
        Date offerStartDate = offer.getStartDate();
        Date offerEndDate = offer.getEndDate();
        String address = offer.getAddress().toString();

        // Match users with offer data
        // A user is considered as matching if he has at least one availability that matches with the offer :
        // - availability start date is before or equals to offer start date
        // - availability end date is after or equals to offer end date
        // - offer address contains a String from availability placeList
        // If a user is matching, he is added to the list of matching users
        List<CandidateUserResponse> matchingUsers = getMatchingUsers(usersAvailabilities, offerStartDate, offerEndDate, address);

        // Get recruiter plan from token (get by id => get plan id => get plan)
        DecodedJWT decodedJWT = JWT.decode(extractToken(token));
        String subField = decodedJWT.getSubject();
        if (subField == null) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No subject in token");
        }
        RecruiterUserResponse requestSender = (RecruiterUserResponse) userService.getUserById(subField);
        if (requestSender == null || !requestSender.getRole().equals(RECRUITER)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid user");
        }
        UUID recruiterPlanId = requestSender.getPlanId();
        if (recruiterPlanId == null) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "No plan associated with recruiter");
        }
        PlanDTO recruiterPlan = makeApiCall(planApiUri + "/" + recruiterPlanId, HttpMethod.GET, PlanDTO.class, token);

        // Return different quantity of users depending on the plan. Candidate information also depends on the plan.
        if (recruiterPlan.getName().equalsIgnoreCase(FREE)) {
            return getFreeUsers(matchingUsers);
        } else if (recruiterPlan.getName().equalsIgnoreCase(SILVER) || recruiterPlan.getName().equalsIgnoreCase(GOLD) || recruiterPlan.getName().equalsIgnoreCase(PLATINUM)) {
            return getPremiumUsers(matchingUsers, recruiterPlan);
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Plan not found");
        }
    }

    /**
     * Get premium users depending on the recruiter plan
     *
     * @param matchingUsers List of users matching with the offer
     * @param recruiterPlan Recruiter plan
     * @return List of premium users
     * @throws HttpClientErrorException if an error occurs while calling the API
     */
    private List<PlanUser> getPremiumUsers(List<CandidateUserResponse> matchingUsers, PlanDTO recruiterPlan) throws HttpClientErrorException {
        List<PlanUser> premiumUsers = new ArrayList<>();

        for (CandidateUserResponse user : matchingUsers) {
            SilverGoldPlatinumUser premiumUser = new SilverGoldPlatinumUser();
            premiumUser.setId(user.getId());
            premiumUser.setFirstName(user.getFirstName());
            premiumUser.setLastName(user.getLastName());
            premiumUser.setProfilePictureUrl(user.getProfilePictureUrl());
            premiumUser.setPhone(user.getPhone());
            premiumUser.setEmail(user.getEmail());
            premiumUsers.add(premiumUser);
        }

        if (recruiterPlan.getName().equalsIgnoreCase(SILVER)) {
            // Get max 10 random users
            Collections.shuffle(premiumUsers);
            if (premiumUsers.size() > 10) {
                premiumUsers = premiumUsers.subList(0, 10);
            }
            return premiumUsers;
        } else if (recruiterPlan.getName().equalsIgnoreCase(GOLD) || recruiterPlan.getName().equalsIgnoreCase(PLATINUM)) {
            return premiumUsers;
        } else {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "Plan not found");
        }
    }

    /**
     * Get free users.
     * Free users are users with only basic information (id, first name, last name, profile picture) and max 5 users are returned.
     *
     * @param matchingUsers List of users matching with the offer
     * @return List of free users
     */
    private List<PlanUser> getFreeUsers(List<CandidateUserResponse> matchingUsers) {
        List<PlanUser> freeUsers = new ArrayList<>();

        for (CandidateUserResponse user : matchingUsers) {
            FreeUser freeUser = new FreeUser();
            freeUser.setId(user.getId());
            freeUser.setFirstName(user.getFirstName());
            freeUser.setLastName(user.getLastName());
            freeUser.setProfilePictureUrl(user.getProfilePictureUrl());
            freeUsers.add(freeUser);
        }

        // Get max 5 random users
        Collections.shuffle(freeUsers);
        if (freeUsers.size() > 5) {
            freeUsers = freeUsers.subList(0, 5);
        }

        return freeUsers;
    }

    /**
     * Create headers for API calls.
     *
     * @param token String - Access token from the user who adds the review
     * @return HttpHeaders
     * @throws HttpClientErrorException if the token is not valid
     */
    private HttpHeaders createHeaders(String token) throws HttpClientErrorException {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token.replace("Bearer ", ""));
        return headers;
    }

    /**
     * Make an API call.
     *
     * @param uri          URI of the API
     * @param method       HTTP method
     * @param responseType Class of the response
     * @param token        String - Access token from the user who adds the review
     * @param <T>          Type of the response
     * @return Response
     * @throws HttpClientErrorException if an error occurs while calling the API
     */
    private <T> T makeApiCall(String uri, HttpMethod method, Class<T> responseType, String token) throws HttpClientErrorException {
        logger.info("Making API call to {}", uri);
        HttpHeaders headers = createHeaders(token);
        HttpEntity<Void> entity = new HttpEntity<>(null, headers);
        ResponseEntity<T> response = restTemplate.exchange(uri, method, entity, responseType);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new HttpClientErrorException(response.getStatusCode());
        }
    }

    /**
     * Get matching users
     *
     * @param usersAvailabilities Map of users and their availabilities
     * @param offerStartDate      Start date of the offer
     * @param offerEndDate        End date of the offer
     * @param address             Address of the offer
     * @return List of users matching with the offer
     */
    private List<CandidateUserResponse> getMatchingUsers(Map<CandidateUserResponse, List<AvailabilityDTO>> usersAvailabilities, Date offerStartDate, Date offerEndDate, String address) {
        logger.info("Getting matching users");
        List<CandidateUserResponse> matchingUsers = new ArrayList<>();

        for (Map.Entry<CandidateUserResponse, List<AvailabilityDTO>> userAvailabilities : usersAvailabilities.entrySet()) {
            for (AvailabilityDTO availability : userAvailabilities.getValue()) {

                // Check if availability start date is before or equals to offer start date
                boolean startDateMatches = availability.getStartDate().before(offerStartDate) || availability.getStartDate().equals(offerStartDate);

                // Check if availability end date is after or equals to offer end date
                boolean endDateMatches = availability.getEndDate().after(offerEndDate) || availability.getEndDate().equals(offerEndDate);

                // Check if offer address is in the availability place list
                boolean placeMatches = false;
                for (String place : availability.getPlaceList()) {
                    if (address.toLowerCase().contains(place.toLowerCase())) {
                        placeMatches = true;
                        break;
                    }
                }

                // Add user to matching list if all criteria are met
                if (startDateMatches && endDateMatches && placeMatches) {
                    matchingUsers.add(userAvailabilities.getKey());
                }
            }
        }

        return matchingUsers;
    }


    /**
     * Get availabilities of a user
     *
     * @param user  User
     * @param token String - Access token from the user who adds the review
     * @return List of availabilities
     * @throws HttpClientErrorException if an error occurs while calling the API
     */
    private List<AvailabilityDTO> getAvailabilities(CandidateUserResponse user, String token) throws HttpClientErrorException {
        if (user.getAvailabilityIdList() == null || user.getAvailabilityIdList().isEmpty()) {
            return Collections.emptyList();
        }

        List<AvailabilityDTO> availabilities = new ArrayList<>();
        for (UUID availabilityId : user.getAvailabilityIdList()) {
            String uri = availabilityApiUri + "/" + availabilityId;
            AvailabilityDTO availability = makeApiCall(uri, HttpMethod.GET, AvailabilityDTO.class, token);
            availabilities.add(availability);
        }
        return availabilities;
    }

    /**
     * Extract token from bearer token.
     *
     * @param token Bearer token.
     * @return Token.
     * @throws HttpClientErrorException if the token is not valid
     */
    private String extractToken(String token) throws HttpClientErrorException {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        return token.replace("Bearer ", "");
    }
}
