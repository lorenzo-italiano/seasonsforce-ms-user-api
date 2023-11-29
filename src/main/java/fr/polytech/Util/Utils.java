package fr.polytech.Util;

import fr.polytech.model.request.UpdateDTO;
import fr.polytech.model.response.user.BaseUserResponse;
import fr.polytech.model.response.user.CandidateUserResponse;
import fr.polytech.model.response.user.RecruiterCandidate;
import fr.polytech.model.response.user.RecruiterUserResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static fr.polytech.constant.Roles.*;

public class Utils {

    private static final List<String> commonAttributes = List.of("birthdate", "citizenship", "phone", "addressId", "profilePictureUrl", "gender", "isRegistered", "toBeRemoved");

    /**
     * Validate attributes of users depending on their role
     *
     * @param updatedUser User to update and to check
     * @param role        User role
     * @throws HttpClientErrorException if the role is invalid or if the attributes are invalid
     */
    public static void validateAttributes(UserRepresentation updatedUser, String role) throws HttpClientErrorException {

        List<String> adminAttributes = new ArrayList<>(List.of("role"));
        List<String> candidateAttributes = new ArrayList<>(List.of("role", "cvUrl", "shortBio", "referenceIdList", "experienceIdList", "availabilityIdList", "reviewIdList"));
        candidateAttributes.addAll(commonAttributes);
        List<String> recruiterAttributes = new ArrayList<>(List.of("role", "companyId", "planId", "offerIdList", "paymentIdList"));
        recruiterAttributes.addAll(commonAttributes);

        Map<String, List<String>> authorizedAttributesMap = new HashMap<>();
        authorizedAttributesMap.put(ADMIN, adminAttributes);
        authorizedAttributesMap.put(CANDIDATE, candidateAttributes);
        authorizedAttributesMap.put(RECRUITER, recruiterAttributes);

        List<String> authorizedAttributes = authorizedAttributesMap.get(role);
        if (authorizedAttributes == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid role");
        }

        if (updatedUser.getAttributes().size() > authorizedAttributes.size()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid attributes for " + role);
        }

        for (String attribute : updatedUser.getAttributes().keySet()) {
            if (!authorizedAttributes.contains(attribute)) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid attributes for " + role);
            }
        }
    }

    /**
     * Convert a UserRepresentation to a UserResponse
     *
     * @param userRepresentation UserRepresentation to convert
     * @return UserResponse converted
     */
    public static BaseUserResponse userRepresentationToUserResponse(UserRepresentation userRepresentation) {
        String role = getStringAttribute(userRepresentation, "role");
        BaseUserResponse userResponse;

        if (CANDIDATE.equals(role)) {
            userResponse = new CandidateUserResponse();
            setCandidateSpecificFields((CandidateUserResponse) userResponse, userRepresentation);
            setCommonFieldsForCandidateAndRecruiter(userResponse, userRepresentation);
        } else if (RECRUITER.equals(role)) {
            userResponse = new RecruiterUserResponse();
            setRecruiterSpecificFields((RecruiterUserResponse) userResponse, userRepresentation);
            setCommonFieldsForCandidateAndRecruiter(userResponse, userRepresentation);
        } else {
            userResponse = new BaseUserResponse();
        }

        setGeneralCommonFields(userResponse, userRepresentation);
        return userResponse;
    }

    /**
     * Set general common fields for all users
     *
     * @param userResponse       UserResponse to set the fields to
     * @param userRepresentation UserRepresentation to get the fields from
     */
    private static void setGeneralCommonFields(BaseUserResponse userResponse, UserRepresentation userRepresentation) {
        userResponse.setId(UUID.fromString(userRepresentation.getId()));
        userResponse.setEmail(userRepresentation.getEmail());
        userResponse.setFirstName(userRepresentation.getFirstName());
        userResponse.setLastName(userRepresentation.getLastName());
        userResponse.setUsername(userRepresentation.getUsername());
        userResponse.setRole(getStringAttribute(userRepresentation, "role"));
    }

    /**
     * Set common fields for Candidate and Recruiter
     *
     * @param baseUserResponse   UserResponse to set the fields to
     * @param userRepresentation UserRepresentation to get the fields from
     */
    private static void setCommonFieldsForCandidateAndRecruiter(BaseUserResponse baseUserResponse, UserRepresentation userRepresentation) {
        if (baseUserResponse instanceof RecruiterCandidate userResponse) {
            userResponse.setBirthdate(stringToDate(getStringAttribute(userRepresentation, "birthdate")));
            userResponse.setCitizenship(getStringAttribute(userRepresentation, "citizenship"));
            userResponse.setPhone(getStringAttribute(userRepresentation, "phone"));
            userResponse.setAddressId(getUUIDAttribute(userRepresentation, "addressId"));
            userResponse.setProfilePictureUrl(getStringAttribute(userRepresentation, "profilePictureUrl"));
            userResponse.setGender(getIntegerAttribute(userRepresentation, "gender"));
            userResponse.setIsRegistered(Boolean.parseBoolean(getStringAttribute(userRepresentation, "isRegistered")));
            userResponse.setToBeRemoved(Boolean.parseBoolean(getStringAttribute(userRepresentation, "toBeRemoved")));
        }
    }

    /**
     * Set Candidate specific fields
     *
     * @param userResponse       UserResponse to set the fields to
     * @param userRepresentation UserRepresentation to get the fields from
     */
    private static void setCandidateSpecificFields(CandidateUserResponse userResponse, UserRepresentation userRepresentation) {
        userResponse.setCvUrl(getStringAttribute(userRepresentation, "cvUrl"));
        userResponse.setShortBio(getStringAttribute(userRepresentation, "shortBio"));
        userResponse.setReferenceIdList(getUUIDListAttribute(userRepresentation, "referenceIdList"));
        userResponse.setExperienceIdList(getUUIDListAttribute(userRepresentation, "experienceIdList"));
        userResponse.setAvailabilityIdList(getUUIDListAttribute(userRepresentation, "availabilityIdList"));
        userResponse.setReviewIdList(getUUIDListAttribute(userRepresentation, "reviewIdList"));
    }

    /**
     * Set Recruiter specific fields
     *
     * @param userResponse       UserResponse to set the fields to
     * @param userRepresentation UserRepresentation to get the fields from
     */
    private static void setRecruiterSpecificFields(RecruiterUserResponse userResponse, UserRepresentation userRepresentation) {
        userResponse.setCompanyId(getUUIDAttribute(userRepresentation, "companyId"));
        userResponse.setPlanId(getUUIDAttribute(userRepresentation, "planId"));
        userResponse.setOfferIdList(getUUIDListAttribute(userRepresentation, "offerIdList"));
        userResponse.setPaymentIdList(getUUIDListAttribute(userRepresentation, "paymentIdList"));
    }

    /**
     * Convert a UpdateBody to a UserRepresentation
     *
     * @param updatedUser UpdateBody to convert
     * @param role        User role
     * @return UserRepresentation converted
     */
    public static UserRepresentation updateBodyToUserRepresentation(UpdateDTO updatedUser, String role) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEmail(updatedUser.getEmail());
        userRepresentation.setFirstName(updatedUser.getFirstName());
        userRepresentation.setLastName(updatedUser.getLastName());
        userRepresentation.setUsername(updatedUser.getUsername());

        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("role", Collections.singletonList(role));
        attributes.put("birthdate", Collections.singletonList(safeToString(updatedUser.getBirthdate())));
        attributes.put("citizenship", Collections.singletonList(updatedUser.getCitizenship()));
        attributes.put("phone", Collections.singletonList(updatedUser.getPhone()));
        attributes.put("addressId", Collections.singletonList(safeToString(updatedUser.getAddressId())));
        attributes.put("profilePictureUrl", Collections.singletonList(updatedUser.getProfilePictureUrl()));
        attributes.put("gender", Collections.singletonList(safeToString(updatedUser.getGender())));
        attributes.put("isRegistered", Collections.singletonList(safeToString(updatedUser.getIsRegistered())));
        attributes.put("toBeRemoved", Collections.singletonList(safeToString(updatedUser.getToBeRemoved())));

        if (CANDIDATE.equals(role)) {
            attributes.put("cvUrl", Collections.singletonList(updatedUser.getCvUrl()));
            attributes.put("shortBio", Collections.singletonList(updatedUser.getShortBio()));
            attributes.put("referenceIdList", Optional.ofNullable(updatedUser.getReferenceIdList()).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toList()));
            attributes.put("experienceIdList", Optional.ofNullable(updatedUser.getExperienceIdList()).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toList()));
            attributes.put("availabilityIdList", Optional.ofNullable(updatedUser.getAvailabilityIdList()).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toList()));
            attributes.put("reviewIdList", Optional.ofNullable(updatedUser.getReviewIdList()).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toList()));
        } else if (RECRUITER.equals(role)) {
            attributes.put("companyId", Collections.singletonList(safeToString(updatedUser.getCompanyId())));
            attributes.put("planId", Collections.singletonList(safeToString(updatedUser.getPlanId())));
            attributes.put("offerIdList", Optional.ofNullable(updatedUser.getOfferIdList()).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toList()));
            attributes.put("paymentIdList", Optional.ofNullable(updatedUser.getPaymentIdList()).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toList()));
        }

        userRepresentation.setAttributes(attributes);
        return userRepresentation;
    }

    /**
     * Get a String attribute from a UserRepresentation
     *
     * @param userRepresentation UserRepresentation to get the attribute from
     * @param attribute          Attribute to get
     * @return String attribute or null if the attribute is not present
     */
    private static String getStringAttribute(UserRepresentation userRepresentation, String attribute) {
        Map<String, List<String>> attributes = userRepresentation.getAttributes();
        if (attributes == null) {
            return null;
        }

        List<String> values = attributes.get(attribute);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    /**
     * Get an Integer attribute from a UserRepresentation
     *
     * @param userRepresentation UserRepresentation to get the attribute from
     * @param attribute          Attribute to get
     * @return Integer attribute or null if the attribute is not present
     */
    private static Integer getIntegerAttribute(UserRepresentation userRepresentation, String attribute) {
        String value = getStringAttribute(userRepresentation, attribute);
        return value != null ? Integer.parseInt(value) : null;
    }

    /**
     * Get a UUID attribute from a UserRepresentation
     *
     * @param userRepresentation UserRepresentation to get the attribute from
     * @param attribute          Attribute to get
     * @return UUID attribute or null if the attribute is not present
     */
    private static UUID getUUIDAttribute(UserRepresentation userRepresentation, String attribute) {
        String value = getStringAttribute(userRepresentation, attribute);
        return value != null ? UUID.fromString(value) : null;
    }

    /**
     * Get a List of UUID attribute from a UserRepresentation
     *
     * @param userRepresentation UserRepresentation to get the attribute from
     * @param attribute          Attribute to get
     * @return List of UUID attribute or null if the attribute is not present
     */
    private static List<UUID> getUUIDListAttribute(UserRepresentation userRepresentation, String attribute) {
        Map<String, List<String>> attributes = userRepresentation.getAttributes();
        if (attributes == null) {
            return null;
        }

        List<String> values = attributes.get(attribute);
        return values != null ? values.stream().map(UUID::fromString).collect(Collectors.toList()) : null;
    }


    /**
     * Convert a String to a Date
     *
     * @param date String to convert
     * @return Date converted
     * @throws HttpClientErrorException if the date is invalid
     */
    private static Date stringToDate(String date) throws HttpClientErrorException {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        if (date == null) {
            return null;
        }
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid date format");
        }
    }

    /**
     * Get a String from an Object
     *
     * @param obj Object to get the String from
     * @return String or null if the Object is null
     */
    private static String safeToString(Object obj) {
        return Optional.ofNullable(obj).map(Object::toString).orElse(null);
    }

}
