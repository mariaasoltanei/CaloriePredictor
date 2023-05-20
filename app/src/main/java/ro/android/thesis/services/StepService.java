package ro.android.thesis.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;
import kotlinx.coroutines.channels.ChannelKt;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.broadcasts.NotificationReceiver;
import ro.android.thesis.domain.StepCount;
import ro.android.thesis.utils.FitnessCalculations;


public class StepService extends Service implements SensorEventListener {
    public static final String STEP_COUNT_ACTION = "ro.android.thesis.services.STEP_COUNT_ACTION";
    public static final String EXTRA_STEP_COUNT = "ro.android.thesis.services.EXTRA_STEP_COUNT";

    public static final String SPEED_ACTION = "ro.android.thesis.services.SPEED_ACTION";
    public static final String SPEED_NUMBER = "ro.android.thesis.services.SPEED_NUMBER";
    public static final String CALORIES_NUMBER = "ro.android.thesis.services.CALORIES_NUMBER";

    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_NORMAL;
    private static final String TAG = "StepCountService";
    private static final double SPEED_EPSILON = 0.0001;
    private boolean isStepServiceRunning;
    private int totalSteps = 0;
    private int stepsToday = 0;
    double speed = 0;
    double calories = 0;
    double totalCalories = 0;

    private SensorManager sensorManager;
    private Sensor stepSensor;

    private final IBinder binder = new StepCountBinder();
    private final ArrayList<StepCount> stepCountList = new ArrayList<>();
    private final ArrayList<StepCount> stepsTodayList = new ArrayList<>();

    private Realm realmStepService;
    private Handler handler;
    private Handler resetStepsHandler;
    private Runnable runnable;
    private Runnable resetStepsRunnable;
    private CalAidApp calAidApp;

    private SyncConfiguration syncConfiguration;
    private User user;
    private String userId;
    private long startTime;
    final String CHANNELID = "Foreground Service ID 2";
    private static final double STRIDE_LENGTH = 0.75;

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
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SENSOR_DELAY);
            stepsToday = totalSteps;
            Log.d(TAG, "stepsToday onStart " + stepsToday);
        } else {
            Log.e(TAG, "onCreate: Step sensor not available");
        }

        Log.d(TAG, "TOTAL STEPS " + totalSteps);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");
        stepCountList.clear();
        stepsTodayList.clear();
        if (intent != null) {
            if (intent.getAction() == "startStepService") {
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
                                sendSpeedAndCalories();
                                fireNotification(getStepCount());
                                //resetStepCount();
                                handler.postDelayed(this, 5000);
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    }
                });

                createNotificationChannel(CHANNELID);
                Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                        .setContentText("Collecting steps..")
                        .setContentTitle("")
                        .setSmallIcon(R.drawable.icon_launcher);

                startForeground(1002, notification.build());
            }
            if (intent.getAction() == "stopStepService") {
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

    private void updateSpeed(double speed, double calories) {
        Intent intent = new Intent(SPEED_ACTION);
        intent.putExtra(SPEED_NUMBER, speed);
        totalCalories += calories;
        intent.putExtra(CALORIES_NUMBER, totalCalories);
        sendBroadcast(intent);
    }

    public void resetStepCount() {
        Calendar midnight = Calendar.getInstance();
        midnight.setTimeInMillis(System.currentTimeMillis());
        midnight.set(Calendar.HOUR_OF_DAY, 14);
        midnight.set(Calendar.MINUTE, 38);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        if(System.currentTimeMillis() == midnight.getTimeInMillis()){
            this.stepsToday = 0;
            Log.d(TAG, "onSensorChanged: Step today: " + stepsToday);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(STEP_COUNT_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
            intent.putExtra(EXTRA_STEP_COUNT, stepsToday);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
//            }
            addStepsToSharedPreferences();
            sendBroadcast(intent);
        }

        //sendBroadcast(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        totalSteps = (int) event.values[0];
        fireNotification(totalSteps - stepsToday);
        updateStepCount(totalSteps - stepsToday);
        StepCount data = new StepCount();
        data.set_id(new ObjectId());
        data.setUserId(userId);
        data.setNoSteps(totalSteps);
        data.setTimestamp(new Date(System.currentTimeMillis()));
        stepCountList.add(data);
        StepCount stepsTodayObj = new StepCount();
        stepsTodayObj.setNoSteps(totalSteps - stepsToday);
        stepsTodayObj.setTimestamp(new Date(System.currentTimeMillis()));
        stepsTodayList.add(stepsTodayObj);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public int getStepCount() {
        return totalSteps - stepsToday;
    }

    public double getSpeed() {

        return speed;
    }

    public double getCalories() {
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        String formattedString = decimalFormat.format(calories);
        return Double.parseDouble(formattedString);

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

    private void sendSpeedAndCalories() {
        if (stepsTodayList.size() > 0) {
            int noStepsInterval = stepsTodayList.size();
            Instant start = stepsTodayList.get(0).getTimestamp().toInstant();
            Instant end = stepsTodayList.get(stepsTodayList.size() - 1).getTimestamp().toInstant();
            Duration duration = Duration.between(start, end);
            double timeIntervalMins = duration.toMillis() / 60000.0;
            double timeIntervalHours = timeIntervalMins / 60.0;
            if (timeIntervalMins > SPEED_EPSILON) {
                NumberFormat formatter = new DecimalFormat("#0.00");
                double stepFrequency = noStepsInterval / timeIntervalMins;
                double speed = Double.parseDouble(formatter.format(stepFrequency * STRIDE_LENGTH));
                double mets = FitnessCalculations.calculateMETSWalking(speed);
                double calories = Double.parseDouble(formatter.format(FitnessCalculations.calculateCalories(timeIntervalHours, mets, getUserWeight())));
                Log.d("Interval-mets", String.valueOf(mets));
                Log.d("Interval-calories", String.valueOf(calories));

                updateSpeed(speed, calories);
            } else {
                updateSpeed(0.0, 0.0);
            }
        } else {
            updateSpeed(0.0, 0.0);
        }
        stepsTodayList.clear();
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

    private double getUserWeight() {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ro.android.thesis.domain.User>() {
        }.getType();
        ro.android.thesis.domain.User user = gson.fromJson(userLogged, type);
        return user.getWeight();

    }

    private void addStepsToSharedPreferences() {
        SharedPreferences sharedPref = this.getSharedPreferences("stepCount", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("numSteps", stepsToday);
        //editor.clear();
        editor.apply();
    }

    private int getStepsFromSharedPreferences() {
        SharedPreferences sharedPref = this.getSharedPreferences("stepCount", Context.MODE_PRIVATE);
        int savedSteps = sharedPref.getInt("numSteps", 0);
        Log.d(TAG, String.valueOf(savedSteps));
        return savedSteps;
    }

    private void fireNotification(int stepCount) {

        if (stepCount == 100) {
            sendNotification("Congrats, first 100 steps!", CHANNELID);
        }
        if (stepCount == 1000 || stepCount == 5000 || stepCount == 8000) {
            String notificationText = "Keep up the good work! You have reached " + stepCount + "steps.";
            sendNotification(notificationText, CHANNELID);
        }
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minutesOfDay = calendar.get(Calendar.MINUTE);
        if (stepCount < 100 && hourOfDay == 16 && minutesOfDay == 05) {
            sendNotification("Time for some exercise!", CHANNELID);
        }
    }

    private void sendNotification(String message , String CHANNELID) {
        // Customize the notification content
        String title = "CalAid";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNELID )
                .setSmallIcon(R.drawable.icon_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(2, builder.build());
    }


public class StepCountBinder extends Binder {
    public StepService getService() {
        return StepService.this;
    }
}
}