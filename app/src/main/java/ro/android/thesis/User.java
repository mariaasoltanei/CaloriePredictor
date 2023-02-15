package ro.android.thesis;

import java.time.LocalDate;

public class User {
    private String firstName;
    private String email;
    private String password;
    private LocalDate registrationDate;

    public User(String firstName, String email, String password, LocalDate registrationDate) {
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
    }
}
