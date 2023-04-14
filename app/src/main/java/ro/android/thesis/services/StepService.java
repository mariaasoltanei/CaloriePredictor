package ro.android.thesis.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.StepCount;
import ro.android.thesis.domain.User;


public class StepService extends Service implements SensorEventListener {
    public static final String STEP_COUNT_ACTION = "ro.android.thesis.services.STEP_COUNT_ACTION";
    public static final String EXTRA_STEP_COUNT = "ro.android.thesis.services.EXTRA_STEP_COUNT";
    private static final String TAG = "StepCountService";
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL;
    private final IBinder binder = new StepCountBinder();
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int totalSteps = 0;
    private int stepsToday = 0;
    private Realm realm;
    private Handler handler;
    private Runnable runnable;
    private final ArrayList<StepCount> stepCountList = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //realm = Realm.getInstance(CalAidApp.getSyncConfigurationMain());
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                //sendStepsToMongoDB();

                handler.postDelayed(this, 30000);
            }
        };
        handler.postDelayed(runnable, 30000);
        Log.d(TAG, "TOTAL STEPS " + String.valueOf(totalSteps));
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SENSOR_DELAY);
            stepsToday = totalSteps;
            Log.d(TAG, "stepsToday onStart " + String.valueOf(stepsToday));
        } else {
            Log.e(TAG, "onCreate: Step sensor not available");
        }
        getStepsFromSharedPreferences();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");

        final String CHANNELID = "Foreground Service ID 2";
        createNotificationChannel(CHANNELID);
        Notification.Builder notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, CHANNELID)
                    .setContentText("Collecting steps..")
                    .setContentTitle("")
                    .setSmallIcon(R.drawable.icon_launcher);
        }
        //getStepsFromSharedPreferences();
        resetStepCount();

        startForeground(1002, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Service destroyed");
        addStepsToSharedPreferences();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
//        realm.close();

    }

    private void updateStepCount(int stepCount) {
        Intent intent = new Intent(STEP_COUNT_ACTION);
        intent.putExtra(EXTRA_STEP_COUNT, stepCount);
        sendBroadcast(intent);
    }

    public void resetStepCount() {
        this.stepsToday = this.totalSteps;
        //updateStepCount(stepsToday);
        Log.d(TAG, "onSensorChanged: Step today: " + stepsToday);
        Calendar midnight = Calendar.getInstance();
        midnight.setTimeInMillis(System.currentTimeMillis());
        midnight.set(Calendar.HOUR_OF_DAY, 00);
        midnight.set(Calendar.MINUTE, 00);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(STEP_COUNT_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        intent.putExtra(EXTRA_STEP_COUNT, stepsToday);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setRepeating(AlarmManager.RTC, midnight.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
        addStepsToSharedPreferences();
        //sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        totalSteps = (int) event.values[0];
        updateStepCount(totalSteps - stepsToday);
        Log.d(TAG, "onSensorChanged: Step count: " + totalSteps);
//        StepCount data = new StepCount();
//        data.set_id(new ObjectId());
//        data.setUserId(CalAidApp.getApp().currentUser().getId());
//        data.setNoSteps(totalSteps);;
//        data.setTimestamp(new Date(System.currentTimeMillis()));
//        stepCountList.add(data);
        Log.d(TAG, "onSensorChanged: Step count: " + totalSteps);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int getStepCount() {
        return totalSteps - stepsToday;
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

    private void sendStepsToMongoDB() {
        if (stepCountList.size() > 0) {
            final ArrayList<StepCount> stepCountSend = new ArrayList<>(stepCountList);
            stepCountList.clear();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insert(stepCountSend);
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Data sent to MongoDB");
                }
            }, new Realm.Transaction.OnError() {
                @Override
                public void onError(Throwable error) {
                    Log.e(TAG, "Error sending data to MongoDB", error);
                }
            });
        }
        else {
            Log.d(TAG, "dASDA");
        }
    }
    private void addStepsToSharedPreferences() {
        SharedPreferences sharedPref = this.getSharedPreferences("stepCount", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("numSteps", stepsToday);
        //editor.clear();
        editor.apply();
    }

    private void getStepsFromSharedPreferences(){
        SharedPreferences sharedPref = this.getSharedPreferences("stepCount", Context.MODE_PRIVATE);
        int savedSteps = sharedPref.getInt("numSteps", 0);
        Log.d(TAG, String.valueOf(savedSteps));
        stepsToday = savedSteps;
    }

    public class StepCountBinder extends Binder {
        public StepService getService() {
            return StepService.this;
        }
    }
}