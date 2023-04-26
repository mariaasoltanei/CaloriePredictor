package ro.android.thesis.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;

import ro.android.thesis.services.StepService;

public class StepCountReceiver extends BroadcastReceiver {
    private TextView countSteps;

    public StepCountReceiver(TextView countSteps) {
        this.countSteps = countSteps;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(StepService.STEP_COUNT_ACTION)) {
            int stepCount = intent.getIntExtra(StepService.EXTRA_STEP_COUNT, 0);
            countSteps.setText(String.valueOf(stepCount));
        }
    }
}
