package fr.polytech.model.response.user.plan;

public class SilverGoldPlatinumUser extends FreeUser implements PlanUser {
    private String email;
    private String phone;

    public SilverGoldPlatinumUser() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
