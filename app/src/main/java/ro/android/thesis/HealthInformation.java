package ro.android.thesis;

public class HealthInformation {
    private int height;
    private double weight;
    private String gender;

    public HealthInformation() {

    }

    public HealthInformation(int height, double weight) {
        this.height = height;
        this.weight = weight;
    }

    public HealthInformation(int height, double weight, String gender) {
        this.height = height;
        this.weight = weight;
        this.gender = gender;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "HealthInformation{" +
                "height=" + height +
                ", weight=" + weight +
                ", gender='" + gender + '\'' +
                '}';
    }
}
