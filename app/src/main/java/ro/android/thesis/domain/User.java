package ro.android.thesis.domain;

import org.bson.types.ObjectId;

import java.time.LocalDate;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class User extends RealmObject {
    @PrimaryKey
    @Required
    private ObjectId _id;
    private String firstName;
    private String email;
    private String password;
    private String birthDate;
    private double height;
    private double weight;
    private String gender;
    private double activityMultiplier;
    private String mongoUserId;

    public User() {
    }

    public User(String firstName, String email, String password, String birthDate, double height, double weight, String gender, double activityMultiplier) {
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.activityMultiplier = activityMultiplier;
    }

    public User(ObjectId _id, String firstName, String email, String password, String birthDate, double height, double weight, String gender, double activityMultiplier) {
        this._id = _id;
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.activityMultiplier = activityMultiplier;
    }

    public User(ObjectId _id, String firstName, String email, String password, String birthDate, double height, double weight, String gender, double activityMultiplier, String mongoUserId) {
        this._id = _id;
        this.firstName = firstName;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.activityMultiplier = activityMultiplier;
        this.mongoUserId = mongoUserId;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", birthDate='" + birthDate + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", gender='" + gender + '\'' +
                ", activityMultiplier=" + activityMultiplier +
                '}';
    }
    public ObjectId getId() { return _id; }
    public void setId(ObjectId _id) { this._id = _id; }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getActivityMultiplier() {
        return activityMultiplier;
    }

    public String getMongoUserId() {
        return mongoUserId;
    }

    public void setMongoUserId(String mongoUserId) {
        this.mongoUserId = mongoUserId;
    }

    public void setActivityMultiplier(double activityMultiplier) {
        this.activityMultiplier = activityMultiplier;
    }
}
