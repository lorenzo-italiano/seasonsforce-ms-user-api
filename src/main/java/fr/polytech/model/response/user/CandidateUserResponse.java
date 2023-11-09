package fr.polytech.model.response.user;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CandidateUserResponse extends BaseUserResponse implements IRecruiterCandidate {
    private Date birthdate;
    private String citizenship;
    private String phone;
    private UUID addressId;
    private String profilePictureUrl;
    private String cvUrl;
    private String shortBio;
    private Integer gender;
    private List<UUID> referenceIdList;
    private List<UUID> experienceIdList;
    private List<UUID> availabilityIdList;
    private List<UUID> reviewIdList;

    public CandidateUserResponse() {
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public String getCitizenship() {
        return citizenship;
    }

    public void setCitizenship(String citizenship) {
        this.citizenship = citizenship;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UUID getAddressId() {
        return addressId;
    }

    public void setAddressId(UUID addressId) {
        this.addressId = addressId;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getCvUrl() {
        return cvUrl;
    }

    public void setCvUrl(String cvUrl) {
        this.cvUrl = cvUrl;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
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
}
