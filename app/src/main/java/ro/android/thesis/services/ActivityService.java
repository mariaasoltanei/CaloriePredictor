package ro.android.thesis.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import io.realm.Realm;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;


public class ActivityService extends Service {
    private static final String TAG = "ActivityService";
    private CalAidApp calAidApp;

    private Handler requestHandler;
    private Runnable requestRunnable;

    public void onCreate() {
        super.onCreate();
        calAidApp = (CalAidApp) getApplicationContext();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");

        if(intent != null){
            if(intent.getAction() == "startActivityService"){
                requestHandler = new Handler();
                requestRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "activity");
                        requestHandler.postDelayed(requestRunnable, 5000);
                    }
                };
                requestHandler.postDelayed(requestRunnable, 5000);
                final String CHANNELID = "Foreground Service ID 5";
                createNotificationChannel(CHANNELID);
                Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                        .setContentText("Collecting ACTIVITIES")
                        .setContentTitle("")
                        .setSmallIcon(R.drawable.icon_launcher);

                startForeground(1001, notification.build());
            }
            if(intent.getAction() == "stopActivityService"){
                stopForeground(true);
                stopSelf();
            }
        }


        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Service destroyed");
        requestHandler.removeCallbacks(requestRunnable);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private void createNotificationChannel(String CHANNEL_ID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CalAidApp";
            String description = "Collecting activities";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
