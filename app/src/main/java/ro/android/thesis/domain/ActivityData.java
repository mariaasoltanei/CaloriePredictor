package ro.android.thesis.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ActivityData implements Parcelable, Cloneable {
    private String activityType;
    private double noCalories;

    public ActivityData(String activityType, double noCalories) {
        this.activityType = activityType;
        this.noCalories = noCalories;
    }

    protected ActivityData(Parcel in) {
        activityType = in.readString();
        noCalories = in.readDouble();
    }

    @Override
    public String toString() {
        return "ActivityData{" +
                "activityType='" + activityType + '\'' +
                ", noCalories=" + noCalories +
                '}';
    }

    public static final Creator<ActivityData> CREATOR = new Creator<ActivityData>() {
        @Override
        public ActivityData createFromParcel(Parcel in) {
            return new ActivityData(in);
        }

        @Override
        public ActivityData[] newArray(int size) {
            return new ActivityData[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(activityType);
        parcel.writeDouble(noCalories);
    }
}
