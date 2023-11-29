package fr.polytech.model.response.user;

import java.util.List;
import java.util.UUID;

public class RecruiterUserResponse extends RecruiterCandidate {
    private UUID companyId;
    private UUID planId;
    private List<UUID> offerIdList;
    private List<UUID> paymentIdList;

    public RecruiterUserResponse() {
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
