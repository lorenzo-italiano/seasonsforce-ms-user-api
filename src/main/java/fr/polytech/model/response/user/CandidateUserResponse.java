package fr.polytech.model.response.user;

import java.util.List;
import java.util.UUID;

public class CandidateUserResponse extends RecruiterCandidate {
    private String cvUrl;
    private String shortBio;
    private List<UUID> referenceIdList;
    private List<UUID> experienceIdList;
    private List<UUID> availabilityIdList;
    private List<UUID> reviewIdList;

    public CandidateUserResponse() {
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

    public List<UUID> getReferenceIdList() {
        return referenceIdList;
    }

    public void setReferenceIdList(List<UUID> referenceIdList) {
        this.referenceIdList = referenceIdList;
    }

    public List<UUID> getExperienceIdList() {
        return experienceIdList;
    }

    public void setExperienceIdList(List<UUID> experienceIdList) {
        this.experienceIdList = experienceIdList;
    }

    public List<UUID> getAvailabilityIdList() {
        return availabilityIdList;
    }

    public void setAvailabilityIdList(List<UUID> availabilityIdList) {
        this.availabilityIdList = availabilityIdList;
    }

    public List<UUID> getReviewIdList() {
        return reviewIdList;
    }

    public void setReviewIdList(List<UUID> reviewIdList) {
        this.reviewIdList = reviewIdList;
    }

    @Override
    public String toString() {
        return "CandidateUserResponse{" +
                "cvUrl='" + cvUrl + '\'' +
                ", shortBio='" + shortBio + '\'' +
                ", referenceIdList=" + referenceIdList +
                ", experienceIdList=" + experienceIdList +
                ", availabilityIdList=" + availabilityIdList +
                ", reviewIdList=" + reviewIdList +
                '}';
    }
}
