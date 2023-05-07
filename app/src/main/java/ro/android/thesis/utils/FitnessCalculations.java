package ro.android.thesis.utils;

public class FitnessCalculations {
    public static double calculateMETSWalking(double speedPerMin) {
        return 0.0272 * speedPerMin + 1.2;
    }
    public static double calculateCalories(double duration, double mets, double userWeight){
        return 1.05 * mets * duration * userWeight;
    }

}
