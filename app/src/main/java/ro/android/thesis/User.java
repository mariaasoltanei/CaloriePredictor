package ro.android.thesis;

import java.time.LocalDate;

public class User {
    private String firstName;
    private String email;
    private String password;
    private String birthDate; //TODO:Change to local date when implememting date picker
    private HealthInformation healthInformation;

    public User(String firstName, String email, String password, String birthDate, HealthInformation healthInformation) {
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.healthInformation = healthInformation;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", healthInformation=" + healthInformation +
                '}';
    }
}
