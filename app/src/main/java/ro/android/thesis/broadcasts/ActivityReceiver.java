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
    public ActivityReceiver(ActivityItemAdapter adapter) {
        activityItemAdapter = adapter;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ActivityService.ACTIVITY_ACTION)) {
            ActivityData activityData = intent.getParcelableExtra(ActivityService.ACTIVITY_TYPE);
            Log.d("ActivityReciver", activityData.toString());
//adauga lista cu Activity Data si reset la 00:00
            List<ActivityData> newData = new ArrayList<>();
            newData.add(activityData);
            activityItemAdapter.updateData(newData);
        }
    }
}
