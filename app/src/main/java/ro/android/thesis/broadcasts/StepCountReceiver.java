package ro.android.thesis.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import java.text.DecimalFormat;

import ro.android.thesis.services.StepService;

public class StepCountReceiver extends BroadcastReceiver {
    private TextView countSteps;
    private TextView tvSpeed;
    private TextView tvCalories;

    public StepCountReceiver(TextView countSteps) {
        this.countSteps = countSteps;
    }

    public StepCountReceiver(TextView countSteps, TextView tvSpeed, TextView tvCalories) {
        this.countSteps = countSteps;
        this.tvSpeed = tvSpeed;
        this.tvCalories = tvCalories;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(StepService.STEP_COUNT_ACTION)) {
            int stepCount = intent.getIntExtra(StepService.EXTRA_STEP_COUNT, 0);
            countSteps.setText(String.valueOf(stepCount));
        }
        if (intent.getAction().equals(StepService.SPEED_ACTION)) {
            double speedNumber = intent.getDoubleExtra(StepService.SPEED_NUMBER, 0);
            double caloriesNumber = intent.getDoubleExtra(StepService.CALORIES_NUMBER, 0);
            DecimalFormat decimalFormat = new DecimalFormat("#0.00");
            String formattedString = decimalFormat.format(caloriesNumber);
            tvSpeed.setText(String.valueOf(speedNumber));
            tvCalories.setText(formattedString);
        }
    }
}
