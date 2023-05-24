package ro.android.thesis.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ActivityData {
    private String activityType;
    private double noCalories;

    public ActivityData(String activityType, double noCalories) {
        this.activityType = activityType;
        this.noCalories = noCalories;
    }

    @Override
    public String toString() {
        return "ActivityData{" +
                "activityType='" + activityType + '\'' +
                ", noCalories=" + noCalories +
                '}';
    }


    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public double getNoCalories() {
        return noCalories;
    }

    public void setNoCalories(double noCalories) {
        this.noCalories = noCalories;
    }

}