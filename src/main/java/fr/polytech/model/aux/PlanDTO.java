package fr.polytech.model.aux;

import org.springframework.web.client.HttpClientErrorException;

import java.util.UUID;

public class PlanDTO {
    private UUID id;

    private String name;

    private String description;

    private Float price;

    private String currency;

    private Integer monthsDuration;

    public PlanDTO() { }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getMonthsDuration() {
        return monthsDuration;
    }

    public void setMonthsDuration(Integer monthsDuration) {
        this.monthsDuration = monthsDuration;
    }

    public void validateAttributes() throws HttpClientErrorException {
        if (name == null || description == null || price == null || currency == null || monthsDuration == null) {
            throw new HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST, "Missing attributes");
        }
    }
}
