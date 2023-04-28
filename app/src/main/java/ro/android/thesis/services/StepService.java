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
import androidx.annotation.RequiresApi;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.StepCount;


public class StepService extends Service implements SensorEventListener {
    public static final String STEP_COUNT_ACTION = "ro.android.thesis.services.STEP_COUNT_ACTION";
    public static final String EXTRA_STEP_COUNT = "ro.android.thesis.services.EXTRA_STEP_COUNT";
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL;
    private static final String TAG = "StepCountService";
    private boolean isStepServiceRunning;
    private int totalSteps = 0;
    private int stepsToday = 0;

    private SensorManager sensorManager;
    private Sensor stepSensor;

    private final IBinder binder = new StepCountBinder();
    private final ArrayList<StepCount> stepCountList = new ArrayList<>();

    private Realm realmStepService;
    private Handler handler;
    private Runnable runnable;
    private CalAidApp calAidApp;

    private SyncConfiguration syncConfiguration;
    private User user;
    private String userId;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        calAidApp = (CalAidApp) getApplicationContext();
       // calAidApp.addObserver(this);
        Log.d(TAG, "TOTAL STEPS " + totalSteps);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");
        stepCountList.clear();
        if(intent != null){
            if(intent.getAction() == "startStepService"){
                isStepServiceRunning = true;
                Realm.getInstanceAsync(calAidApp.getSyncConfigurationMain(), new Realm.Callback() {
                    @Override
                    public void onSuccess(Realm realm) {
                        realmStepService = realm;
                        userId = calAidApp.getAppUser().getId();
                        handler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.d("CALAIDAPP -Step service", "steps");
                                //sendStepsToMongoDB();

                                handler.postDelayed(this, 10000);
                            }
                        };
                        handler.postDelayed(runnable, 10000);
                    }
                });
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                if (stepSensor != null) {
                    sensorManager.registerListener(this, stepSensor, SENSOR_DELAY);
                    stepsToday = totalSteps;
                    Log.d(TAG, "stepsToday onStart " + stepsToday);
                } else {
                    Log.e(TAG, "onCreate: Step sensor not available");
                }
                getStepsFromSharedPreferences();

                final String CHANNELID = "Foreground Service ID 2";
                createNotificationChannel(CHANNELID);
                Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                        .setContentText("Collecting steps..")
                        .setContentTitle("")
                        .setSmallIcon(R.drawable.icon_launcher);

                resetStepCount();

                startForeground(1002, notification.build());
            }
            if(intent.getAction() == "stopStepService"){
                isStepServiceRunning = false;
                stopForeground(true);
                stopSelf();
            }
        }

        return START_NOT_STICKY;
}

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Service destroyed");
        super.onDestroy();
        addStepsToSharedPreferences();

        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
        realmStepService.close();
        //calAidApp.removeObserver(this);
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
        midnight.set(Calendar.HOUR_OF_DAY, 13);
        midnight.set(Calendar.MINUTE, 41);
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
        StepCount data = new StepCount();
        data.set_id(new ObjectId());
        data.setUserId(userId);
        data.setNoSteps(totalSteps);
        data.setTimestamp(new Date(System.currentTimeMillis()));
        stepCountList.add(data);
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
            realmStepService.executeTransactionAsync(new Realm.Transaction() {
                                                         @Override
                                                         public void execute(Realm realm) {
                                                             realm.insert(stepCountSend);
                                                         }
                                                     }, () -> Log.d("StepService", "Data sent to MongoDB"),
                    error -> Log.e("StepService", "Error sending data to MongoDB", error));
        }

    }

    private void addStepsToSharedPreferences() {
        SharedPreferences sharedPref = this.getSharedPreferences("stepCount", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("numSteps", stepsToday);
        //editor.clear();
        editor.apply();
    }

    private void getStepsFromSharedPreferences() {
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