package fr.polytech.model.aux;

import fr.polytech.model.response.user.RecruiterUserResponse;

import java.util.UUID;

public class DetailedReferenceDTO {
    private UUID id;
    private String contactName;
    private CompanyDTO company;
    private RecruiterUserResponse contact;
    private String contactJobTitle;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public CompanyDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyDTO company) {
        this.company = company;
    }

    public RecruiterUserResponse getContact() {
        return contact;
    }

    public void setContact(RecruiterUserResponse contact) {
        this.contact = contact;
    }

    public String getContactJobTitle() {
        return contactJobTitle;
    }

    public void setContactJobTitle(String contactJobTitle) {
        this.contactJobTitle = contactJobTitle;
    }
}

