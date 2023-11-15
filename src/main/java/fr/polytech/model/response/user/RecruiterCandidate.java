package fr.polytech.model.response.user;

import java.util.Date;
import java.util.UUID;

public abstract class RecruiterCandidate extends BaseUserResponse {
    private Integer gender;
    private Date birthdate;
    private String citizenship;
    private String phone;
    private UUID addressId;
    private String profilePictureUrl;
    private Boolean toBeRemoved;

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
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

    public Boolean getToBeRemoved() {
        return toBeRemoved;
    }

    public void setToBeRemoved(Boolean toBeRemoved) {
        this.toBeRemoved = toBeRemoved;
    }
}
