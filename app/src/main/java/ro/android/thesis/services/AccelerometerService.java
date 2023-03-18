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
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import org.bson.types.ObjectId;

import io.realm.Realm;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.AccelerometerData;
import ro.android.thesis.fragments.HealthInfoFragment;

public class AccelerometerService extends Service implements SensorEventListener {
    private static final String API_KEY = "nJpGv1efocEzWAmkOSRpkcQWywGCWEyLKNJJRK9XtwzOd5aiSWwcYicb305vypDw";
    io.realm.mongodb.User mongoDBUser;
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Realm realm;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(() -> { //TODO: ADD WHILE TRUE TO CONTINUOUSLY COLLECT SENSOR DATA
                realm = Realm.getInstance(HealthInfoFragment.getSyncConfiguration());
                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

                Log.d("RealmService", realm.getPath());
                Log.d("RealmService", "Initialization succeeded APP");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
               // Log.e("RealmService", "Initialization failed: " + e.getMessage());


        }
        ).start();

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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values[2] < 9.70 && sensorEvent.values[2] > 9.80) {
            realm.executeTransaction(realm -> {
                AccelerometerData data = new AccelerometerData();
                data.setId(new ObjectId());
                data.setUserId(CalAidApp.getCurrentUser().getId());

            });
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
