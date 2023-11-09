package fr.polytech.model.response.user;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RecruiterUserResponse extends BaseUserResponse implements IRecruiterCandidate {
    private Date birthdate;
    private String citizenship;
    private String phone;
    private UUID addressId;
    private String profilePictureUrl;
    private Integer gender;
    private UUID companyId;
    private UUID planId;
    private List<UUID> offerIdList;
    private List<UUID> paymentIdList;

    public RecruiterUserResponse() {
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

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
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
}
