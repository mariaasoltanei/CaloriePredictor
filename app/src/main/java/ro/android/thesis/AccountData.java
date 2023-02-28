package ro.android.thesis;

import java.time.LocalDate;

public interface AccountData {
    User passAccountData(String firstName, String email, String password, String birthDate);
}
