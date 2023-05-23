package ro.android.thesis.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ro.android.thesis.ActivityItemAdapter;
import ro.android.thesis.domain.ActivityData;
import ro.android.thesis.services.ActivityService;
import ro.android.thesis.services.StepService;

public class ActivityReceiver extends BroadcastReceiver {
    private ActivityItemAdapter activityItemAdapter;
    List<ActivityData> activityDataList;

    public ActivityReceiver(ActivityItemAdapter adapter) {
        activityItemAdapter = adapter;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ActivityService.ACTIVITY_ACTION)) {
            double noCalories = intent.getDoubleExtra(ActivityService.NO_CALORIES, 0);
            String activityType = intent.getStringExtra(ActivityService.ACTIVITY_TYPE);
            Log.d("ActivityReciver", activityType);
//adauga lista cu Activity Data si reset la 00:00
//            List<ActivityData> newData = new ArrayList<>();
//            newData.add(new ActivityData(activityType, noCalories));
            activityItemAdapter.updateData(new ActivityData(activityType, noCalories));
        }
    }
}
