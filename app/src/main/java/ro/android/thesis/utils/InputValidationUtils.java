package ro.android.thesis.utils;

import java.util.regex.Pattern;

public class InputValidationUtils {

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        boolean hasUpperCase = false;
        boolean hasSpecialChar = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (isSpecialChar(c)) {
                hasSpecialChar = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            }
        }

        return hasUpperCase && hasSpecialChar && hasNumber;
    }

    private static boolean isSpecialChar(char c) {
        String specialChars = "!@#$%&^()_+-=[]{}|<>?";
        return specialChars.indexOf(c) != -1;
    }


    public static boolean isValidEmail(String email){
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }
}
