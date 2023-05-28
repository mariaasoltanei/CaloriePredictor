package ro.android.thesis.utils;

import android.util.Log;

import java.util.Calendar;

import ro.android.thesis.domain.User;

public class FitnessCalculations {
    public static double calculateMETSWalking(double speedPerMin) {
        return 0.0272 * speedPerMin + 1.2;
    }
    public static double calculateCalories(double duration, double mets, double userWeight){
        return 1.05 * mets * duration * userWeight;
    }
    public static double calculateBMR(User user){
        Calendar calendar = Calendar.getInstance();
        int age = calendar.get(Calendar.YEAR) - Integer.parseInt(user.getBirthDate().substring(user.getBirthDate().length() - 4));
        if(user.getGender() == "Female"){
            return  10 * user.getWeight() + 6.25 * user.getHeight() - 5 * age - 161;
        }
        else{
            return  10 * user.getWeight() + 6.25 * user.getHeight() - 5 * age + 5;
        }
    }

}
