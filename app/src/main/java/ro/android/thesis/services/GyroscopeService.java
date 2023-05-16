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
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.AccelerometerData;
import ro.android.thesis.domain.GyroscopeData;

public class GyroscopeService extends Service implements SensorEventListener {
    private final String TAG = "GyroscopeService";
    private final ArrayList<GyroscopeData> gyroDataList = new ArrayList<>();
    private CalAidApp calAidApp;
    private String userId;
    private SensorManager sensorManager;
    private Sensor sensorGyroscope;

    private Realm realmGyroService;
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        calAidApp = (CalAidApp) getApplicationContext();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");
        gyroDataList.clear();
        if (intent != null) {
            if (intent.getAction() == "startGyroService") {
                Realm.getInstanceAsync(calAidApp.getSyncConfigurationMain(), new Realm.Callback() {
                    @Override
                    public void onSuccess(Realm realm) {
                        realmGyroService = realm;
                        userId = calAidApp.getAppUser().getId();
                        handler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.d("CALAIDAPP -gyro service", "gyro");
                                //sendDataToMongoDB();
                                handler.postDelayed(this, 5000);
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    }
                });
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                sensorManager.registerListener(this, sensorGyroscope, SensorManager.SENSOR_DELAY_NORMAL);


                final String CHANNELID = "Foreground Service ID 3";
                createNotificationChannel(CHANNELID);
                Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                        .setContentText("Collecting gyroscope data...")
                        .setContentTitle("")
                        .setSmallIcon(R.drawable.icon_launcher);

                startForeground(1001, notification.build());
            }
            if (intent.getAction() == "stopGyroService") {
                //isAccServiceRunning = false;
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
        realmGyroService.close();
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, +sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2]);
        GyroscopeData data = new GyroscopeData();
        data.setId(new ObjectId());
        data.setUserId(userId);
        data.setX(sensorEvent.values[0]);
        data.setY(sensorEvent.values[1]);
        data.setZ(sensorEvent.values[2]);
        data.setTimestamp(new Date(System.currentTimeMillis()));
        gyroDataList.add(data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void sendDataToMongoDB() {
        if (gyroDataList.size() > 0) {
            final ArrayList<GyroscopeData> dataToSend = new ArrayList<>(gyroDataList);
            gyroDataList.clear();
            realmGyroService.executeTransactionAsync(new Realm.Transaction() {
                                                                  @Override
                                                                  public void execute(Realm realm) {
                                                                      realm.insert(dataToSend);
                                                                  }
                                                              }, () -> Log.d(TAG, "Data sent to MongoDB"),
                    error -> Log.e(TAG, "Error sending data to MongoDB", error));
        }
    }

    private void createNotificationChannel(String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CalAidApp";
            String description = "Collecting gyroscope data";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
