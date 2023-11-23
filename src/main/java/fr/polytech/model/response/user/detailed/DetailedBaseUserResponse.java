package fr.polytech.model.response.user.detailed;

import java.util.UUID;

public class DetailedBaseUserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String role;
    private Boolean isRegistered;

    public DetailedBaseUserResponse() {
    }

    public DetailedBaseUserResponse(UUID id, String email, String firstName, String lastName, String username, String role, Boolean isRegistered) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.role = role;
        this.isRegistered = isRegistered;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    public void setIsRegistered(Boolean isRegistered) {
        this.isRegistered = isRegistered;
    }
}
