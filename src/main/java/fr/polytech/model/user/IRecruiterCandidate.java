package fr.polytech.model.user;

import java.util.Date;
import java.util.UUID;

public interface IRecruiterCandidate {
    void setGender(Integer gender);
    void setBirthdate(Date birthdate);
    void setCitizenship(String citizenship);
    void setPhone(String phone);
    void setAddressId(UUID addressId);
    void setProfilePictureUrl(String profilePictureUrl);
}
