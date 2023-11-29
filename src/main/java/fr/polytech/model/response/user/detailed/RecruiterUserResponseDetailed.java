package fr.polytech.model.response.user.detailed;

import fr.polytech.model.aux.AddressDTO;
import fr.polytech.model.aux.OfferDetailDTO;
import fr.polytech.model.aux.PaymentDTO;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RecruiterUserResponseDetailed extends DetailedRecruiterCandidate {
    private UUID companyId;
    private UUID planId;
    private List<OfferDetailDTO> offerIdList;
    private List<PaymentDTO> paymentIdList;

    public RecruiterUserResponseDetailed(UUID id, String email, String firstName, String lastName, String username, String role, Boolean isRegistered, Integer gender, Date birthdate, String citizenship, String phone, AddressDTO address, String profilePictureUrl, Boolean toBeRemoved, UUID companyId, UUID planId, List<OfferDetailDTO> offerIdList, List<PaymentDTO> paymentIdList) {
        super(id, email, firstName, lastName, username, role, isRegistered, gender, birthdate, citizenship, phone, address, profilePictureUrl, toBeRemoved);
        this.companyId = companyId;
        this.planId = planId;
        this.offerIdList = offerIdList;
        this.paymentIdList = paymentIdList;
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

    public List<OfferDetailDTO> getOfferIdList() {
        return offerIdList;
    }

    public void setOfferIdList(List<OfferDetailDTO> offerIdList) {
        this.offerIdList = offerIdList;
    }

    public List<PaymentDTO> getPaymentIdList() {
        return paymentIdList;
    }

    public void setPaymentIdList(List<PaymentDTO> paymentIdList) {
        this.paymentIdList = paymentIdList;
    }
}
