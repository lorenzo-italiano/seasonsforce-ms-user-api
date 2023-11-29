package fr.polytech.model.aux;

import java.util.List;
import java.util.UUID;

public class CompanyDTO {
    private UUID id;
    private String name;
    private String logoUrl;
    private String description;
    private String employeesNumberRange;
    private UUID addressId;
    private String siretNumber;
    private List<String> documentsUrl;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmployeesNumberRange() {
        return employeesNumberRange;
    }

    public void setEmployeesNumberRange(String employeesNumberRange) {
        this.employeesNumberRange = employeesNumberRange;
    }

    public UUID getAddressId() {
        return addressId;
    }

    public void setAddressId(UUID addressId) {
        this.addressId = addressId;
    }

    public String getSiretNumber() {
        return siretNumber;
    }

    public void setSiretNumber(String siretNumber) {
        this.siretNumber = siretNumber;
    }

    public List<String> getDocumentsUrl() {
        return documentsUrl;
    }

    public void setDocumentsUrl(List<String> documentsUrl) {
        this.documentsUrl = documentsUrl;
    }
}
