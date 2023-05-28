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
import androidx.annotation.RequiresApi;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.AccelerometerData;

public class AccelerometerService extends Service implements SensorEventListener {
    private static final String TAG = "AccelerometerService";
    private final ArrayList<AccelerometerData> accelerometerDataList = new ArrayList<>();
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;

    private Realm realmAccelerometerService;
    private Handler handler;
    private Runnable runnable;

    private CalAidApp calAidApp;

    private String userId;

    @Override
    public void onCreate() {
        super.onCreate();
        calAidApp = (CalAidApp) getApplicationContext();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");
        accelerometerDataList.clear();
        if(intent != null){
            if(intent.getAction() == "startAccService"){
                Realm.getInstanceAsync(calAidApp.getSyncConfigurationMain(), new Realm.Callback() {
                    @Override
                    public void onSuccess(Realm realm) {
                        realmAccelerometerService = realm;
                        userId = calAidApp.getAppUser().getId();
                        handler = new Handler();
                        runnable = new Runnable() {
                            @Override
                            public void run() {
                                Log.d("CALAIDAPP -Acc service", "acc");
                                //sendDataToMongoDB();
                                handler.postDelayed(this, 5000);
                            }
                        };
                        handler.postDelayed(runnable, 5000);
                    }
                });
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);


                final String CHANNELID = "Foreground Service ID";
                createNotificationChannel(CHANNELID);
                Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                        .setContentText("Collecting accelerometer data...")
                        .setContentTitle("")
                        .setSmallIcon(R.drawable.icon_launcher);

                startForeground(1001, notification.build());
            }
            if(intent.getAction() == "stopAccService"){
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
        realmAccelerometerService.close();
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
        Log.d(TAG,+ sensorEvent.values[0] + " " + sensorEvent.values[1] + " " + sensorEvent.values[2]);

        AccelerometerData data = new AccelerometerData();
        data.setId(new ObjectId());
        data.setUserId(userId);
        data.setX(sensorEvent.values[0]);
        data.setY(sensorEvent.values[1]);
        data.setZ(sensorEvent.values[2]);
        data.setTimestamp(new Date(System.currentTimeMillis()));
        accelerometerDataList.add(data);
    }

    //TODO: send data even if the 10 seconds did not pass -  send what is left in the array
    private void sendDataToMongoDB() {
        if (accelerometerDataList.size() > 0) {
            final ArrayList<AccelerometerData> dataToSend = new ArrayList<>(accelerometerDataList);
            accelerometerDataList.clear();
            realmAccelerometerService.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.insert(dataToSend);
                }
            }, () -> Log.d(TAG, "ACC sent to MongoDB"),
                    error -> Log.e(TAG, "Error sending data to MongoDB", error));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void createNotificationChannel(String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CalAidApp";
            String description = "Collecting accelerometer data";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
