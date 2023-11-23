package fr.polytech.model.response.user.detailed;

import fr.polytech.model.aux.AddressDTO;

import java.util.Date;
import java.util.UUID;

public abstract class DetailedRecruiterCandidate extends DetailedBaseUserResponse {
    private Integer gender;
    private Date birthdate;
    private String citizenship;
    private String phone;
    private AddressDTO address;
    private String profilePictureUrl;
    private Boolean toBeRemoved;

    public DetailedRecruiterCandidate(UUID id, String email, String firstName, String lastName, String username, String role, Boolean isRegistered, Integer gender, Date birthdate, String citizenship, String phone, AddressDTO address, String profilePictureUrl, Boolean toBeRemoved) {
        super(id, email, firstName, lastName, username, role, isRegistered);
        this.gender = gender;
        this.birthdate = birthdate;
        this.citizenship = citizenship;
        this.phone = phone;
        this.address = address;
        this.profilePictureUrl = profilePictureUrl;
        this.toBeRemoved = toBeRemoved;
    }

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

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
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
