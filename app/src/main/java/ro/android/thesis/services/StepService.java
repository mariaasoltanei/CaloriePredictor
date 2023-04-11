package ro.android.thesis.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import ro.android.thesis.R;
import ro.android.thesis.utils.ResetStepCountTask;

public class StepService extends Service implements SensorEventListener {
    private static final String TAG = "StepCountService";
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount = 0;
    private int initialStepCount = 0;
    int stepsSinceServiceStarted;
    Timer resetTimer = new Timer();

    public static final String STEP_COUNT_ACTION = "ro.android.thesis.services.STEP_COUNT_ACTION";
    public static final String EXTRA_STEP_COUNT = "ro.android.thesis.services.EXTRA_STEP_COUNT";

    private final IBinder binder = new StepCountBinder();

    public class StepCountBinder extends Binder {
        public StepService getService() {
            return StepService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            initialStepCount = stepCount;
            sensorManager.registerListener(this, stepSensor, SENSOR_DELAY);
        } else {
            Log.e(TAG, "onCreate: Step sensor not available");
        }
        final String CHANNELID = "Foreground Service ID 2";
        createNotificationChannel(CHANNELID);
        Notification.Builder notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Collecting steps..")
                    .setContentTitle("")
                    .setSmallIcon(R.drawable.icon_launcher);
        }

        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 20);
        midnight.set(Calendar.MINUTE,53);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        resetTimer.scheduleAtFixedRate(new ResetStepCountTask(this), midnight.getTime(), TimeUnit.DAYS.toMillis(1));

        startForeground(1002, notification.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SENSOR_DELAY);
        } else {
            Log.e(TAG, "onCreate: Step sensor not available");
        }
        final String CHANNELID = "Foreground Service ID 2";
        createNotificationChannel(CHANNELID);
        Notification.Builder notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Collecting steps..")
                    .setContentTitle("")
                    .setSmallIcon(R.drawable.icon_launcher);
        }

        startForeground(1002, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Service destroyed");
        sensorManager.unregisterListener(this);
        resetTimer.cancel();
    }

    private void updateStepCount(int stepCount) {
        Intent intent = new Intent(STEP_COUNT_ACTION);
        intent.putExtra(EXTRA_STEP_COUNT, stepCount);
        sendBroadcast(intent);
    }
    public void resetStepCount() {
        this.initialStepCount = this.stepCount;
        updateStepCount(initialStepCount);
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        stepCount = (int) event.values[0];
        updateStepCount(stepCount - initialStepCount);
        Log.d(TAG, "onSensorChanged: Step count: " + stepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int getStepCount() {
        return stepCount - initialStepCount;
    }
    private void createNotificationChannel(String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CalAidApp";
            String description = "Collecting steps";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}