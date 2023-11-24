package fr.polytech.model;

import java.util.UUID;

public class ReferenceDTO {
    private UUID id;
    private String contact;
    private UUID companyId;
    private UUID contactId;
    private String contactJobTitle;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public UUID getContactId() {
        return contactId;
    }

    public void setContactId(UUID contactId) {
        this.contactId = contactId;
    }

    public String getContactJobTitle() {
        return contactJobTitle;
    }

    public void setContactJobTitle(String contactJobTitle) {
        this.contactJobTitle = contactJobTitle;
    }

    @Override
    public String toString() {
        return "ReferenceDTO{" +
                "id=" + id +
                ", contact='" + contact + '\'' +
                ", companyId=" + companyId +
                ", contactId=" + contactId +
                ", contactJobTitle='" + contactJobTitle + '\'' +
                '}';
    }
}
