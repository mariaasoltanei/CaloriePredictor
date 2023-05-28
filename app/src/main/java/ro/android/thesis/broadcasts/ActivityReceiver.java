package ro.android.thesis.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import java.text.DecimalFormat;
import ro.android.thesis.services.ActivityService;

public class ActivityReceiver extends BroadcastReceiver {

    private TextView tvCaloriesActivity;

    public ActivityReceiver(TextView tvCaloriesActivity) {
        this.tvCaloriesActivity = tvCaloriesActivity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ActivityService.ACTIVITY_ACTION)) {
            double noCalories = intent.getDoubleExtra(ActivityService.NO_CALORIES, 0);
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            Log.d("ActivityReceiver", String.valueOf(noCalories));
            String formattedString = decimalFormat.format(noCalories);
            tvCaloriesActivity.setText(formattedString + " kcal");
        }
    }
}