package fr.polytech.model.request;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UpdateDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private Date birthdate;
    private String citizenship;
    private String phone;
    private UUID addressId;
    private String profilePictureUrl;
    private Integer gender;
    private Boolean isRegistered;

    // Specific to recruiter
    private UUID companyId;
    private UUID planId;
    private List<UUID> offerIdList;
    private List<UUID> paymentIdList;

    // Specific to candidate
    private String cvUrl;
    private String shortBio;
    private List<UUID> referenceIdList;
    private List<UUID> experienceIdList;
    private List<UUID> availabilityIdList;
    private List<UUID> reviewIdList;

    public UpdateDTO() { }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(Boolean registered) {
        isRegistered = registered;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getPlanId() {
        return planId;
    }

    public void setPlanId(UUID planId) {
        this.planId = planId;
    }

    public List<UUID> getOfferIdList() {
        return offerIdList;
    }

    public void setOfferIdList(List<UUID> offerIdList) {
        this.offerIdList = offerIdList;
    }

    public List<UUID> getPaymentIdList() {
        return paymentIdList;
    }

    public void setPaymentIdList(List<UUID> paymentIdList) {
        this.paymentIdList = paymentIdList;
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
}
