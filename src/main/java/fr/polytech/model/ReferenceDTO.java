package fr.polytech.model;

import java.util.UUID;

public class ReferenceDTO {
    private UUID id;
    private String message;
    private UUID companyId;
    private UUID senderId;
    private String senderJobTitle;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public String getSenderJobTitle() {
        return senderJobTitle;
    }

    public void setSenderJobTitle(String senderJobTitle) {
        this.senderJobTitle = senderJobTitle;
    }
}
