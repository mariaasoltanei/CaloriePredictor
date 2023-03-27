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

import java.util.Date;

import io.realm.Realm;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.AccelerometerData;
import ro.android.thesis.fragments.HealthInfoFragment;

public class AccelerometerService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Realm realm;
    private Handler handler;
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        realm = Realm.getInstance(CalAidApp.getSyncConfigurationMain());
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        handler = new Handler();
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//                sendDataToMongoDB();
//                handler.postDelayed(this, 10000); // Send data every 10 seconds
//            }
//        };
//        handler.postDelayed(runnable, 10000); // Start sending data after 10 seconds

    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        final String CHANNELID = "Foreground Service ID";
        createNotificationChannel(CHANNELID);
        Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                .setContentText("Collecting accelerometer data...")
                .setContentTitle("")
                .setSmallIcon(R.drawable.icon_launcher);

        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
        sensorManager.unregisterListener(this);
        //handler.removeCallbacks(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Log.d("AccelerometerService", "AccelerometerService/" + sensorEvent.values[0] +" " + sensorEvent.values[1] +" "+sensorEvent.values[2]);
//        if(realm !=  null) {
//            if (Math.abs(sensorEvent.values[0]) < 0.25 && Math.abs(sensorEvent.values[1]) < 0.6 && sensorEvent.values[2] < 9.70 && sensorEvent.values[2] > 9.90) {
//                realm.executeTransactionAsync(realm -> {
//                    AccelerometerData data = new AccelerometerData();
//                    data.setId(new ObjectId());
//                    data.setUserId(CalAidApp.getApp().currentUser().getId());
//                    data.setX(sensorEvent.values[0]);
//                    data.setY(sensorEvent.values[1]);
//                    data.setZ(sensorEvent.values[2]);
//                    data.setTimestamp(new Date(System.currentTimeMillis()));
//                    Log.d("Realm", "AccelerometerService/" + sensorEvent.values[2]);
//                    realm.insert(data);
//                });
//            }
//        }

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
