package fr.polytech.model.response.user.detailed;

import fr.polytech.model.AvailabilityDTO;
import fr.polytech.model.ExperienceDTO;
import fr.polytech.model.ReferenceDTO;
import fr.polytech.model.ReviewDTO;
import fr.polytech.model.aux.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CandidateUserResponseDetailed extends DetailedRecruiterCandidate {
    private String cvUrl;
    private String shortBio;
    private List<DetailedReferenceDTO> referenceList;
    private List<DetailedExperienceDTO> experienceList;
    private List<DetailedAvailabilityDTO> availabilityList;
    private List<DetailedReviewDTO> reviewList;

    public CandidateUserResponseDetailed(UUID id, String email, String firstName, String lastName, String username, String role, Boolean isRegistered, Integer gender, Date birthdate, String citizenship, String phone, AddressDTO address, String profilePictureUrl, Boolean toBeRemoved, String cvUrl, String shortBio, List<DetailedReferenceDTO> referenceList, List<DetailedExperienceDTO> experienceList, List<DetailedAvailabilityDTO> availabilityList, List<DetailedReviewDTO> reviewList) {
        super(id, email, firstName, lastName, username, role, isRegistered, gender, birthdate, citizenship, phone, address, profilePictureUrl, toBeRemoved);
        this.cvUrl = cvUrl;
        this.shortBio = shortBio;
        this.referenceList = referenceList;
        this.experienceList = experienceList;
        this.availabilityList = availabilityList;
        this.reviewList = reviewList;
    }

    public String getCvUrl() {
        return cvUrl;
    }

    public void setCvUrl(String cvUrl) {
        this.cvUrl = cvUrl;
    }

    public String getShortBio() {
        return shortBio;
    }

    public void setShortBio(String shortBio) {
        this.shortBio = shortBio;
    }

    public List<DetailedReferenceDTO> getReferenceList() {
        return referenceList;
    }

    public void setReferenceList(List<DetailedReferenceDTO> referenceList) {
        this.referenceList = referenceList;
    }

    public List<DetailedExperienceDTO> getExperienceList() {
        return experienceList;
    }

    public void setExperienceList(List<DetailedExperienceDTO> experienceList) {
        this.experienceList = experienceList;
    }

    public List<DetailedAvailabilityDTO> getAvailabilityList() {
        return availabilityList;
    }

    public void setAvailabilityList(List<DetailedAvailabilityDTO> availabilityList) {
        this.availabilityList = availabilityList;
    }

    public List<DetailedReviewDTO> getReviewList() {
        return reviewList;
    }

    public void setReviewList(List<DetailedReviewDTO> reviewList) {
        this.reviewList = reviewList;
    }
}
