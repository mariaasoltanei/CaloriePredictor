package ro.android.thesis.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.ActivityData;
import ro.android.thesis.domain.User;


public class ActivityService extends Service {
    private static final String TAG = "ActivityService";
    public static final String ACTIVITY_ACTION = "ro.android.thesis.services.ACTIVITY_ACTION";
    public static final String NO_CALORIES = "ro.android.thesis.services.NO_CALORIES";
    public static final String ACTIVITY_TYPE = "ro.android.thesis.services.ACTIVITY_TYPE";
    private CalAidApp calAidApp;
    private String url = " http://192.168.0.108:5000/calories/"+ CalAidApp.getApp().currentUser().getId();
    private String urlTest = "http://94.245.91.135:5000/calories/"+ CalAidApp.getApp().currentUser().getId();
    List<ActivityData> activityDataList;
    private double calories;
    private double totalCaloriesActivity = 0;
    private String activity;

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    private Handler requestHandler;
    private Runnable requestRunnable;

    public void onCreate() {
        super.onCreate();
        calAidApp = (CalAidApp) getApplicationContext();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: Service started");
        activityDataList = new ArrayList<>();
        if(intent != null){
            if(intent.getAction() == "startActivityService"){
                requestHandler = new Handler();
                requestRunnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "activity");
                        postRequest(urlTest);
                        requestHandler.postDelayed(requestRunnable, 240000);
                    }
                };
                requestHandler.postDelayed(requestRunnable, 240000 );//300000 120000
                final String CHANNELID = "Foreground Service ID 5";
                createNotificationChannel(CHANNELID);
                Notification.Builder notification = new Notification.Builder(this, CHANNELID)
                        .setContentText("Collecting data from motion sensors ...")
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

    private RequestBody buildRequestBody(JSONObject jsonObject) {
        String jsonStr = jsonObject.toString();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonStr, mediaType);
        return requestBody;
    }

    private void postRequest(String URL){
        try{
            JSONObject jsonObject = new JSONObject();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            Date currentDate = new Date();
            String currentDateString = dateFormat.format(currentDate);
            jsonObject.put("timestamp", currentDateString);
            jsonObject.put("userWeight", getUserWeight());
            RequestBody requestBody = buildRequestBody(jsonObject);
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request
                    .Builder()
                    .post(requestBody)
                    .url(URL)
                    .build();

            Log.d("OkHTTTP-req", requestBody.toString());
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("OkHTTTP", e.getMessage());
                            call.cancel();
                        }
                    }).start();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        double caloriesResponse = jsonObject.getDouble("calories");
                        String activityResponse = jsonObject.getString("activity");
                        NumberFormat formatter = new DecimalFormat("#0.00");
                        setActivity(activityResponse);
                        setCalories(Double.parseDouble(formatter.format(caloriesResponse)));
                        activityDataList.add(new ActivityData(activity, calories));
                        sendActivity(calories);
                        addActivitySharedPrefs();

                        Log.d("OkHTTTPpost", String.valueOf(calories));
                        Log.d("OkHTTTPpost", String.valueOf(activity));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    //TODO: CU ASTA SA TRIMIT NR CALORII DIN REQUEST
    private void sendActivity(double noCaloriesActivity) {
        Intent intent = new Intent(ACTIVITY_ACTION);
        totalCaloriesActivity += noCaloriesActivity;
        NumberFormat formatter = new DecimalFormat("#0.00");
        intent.putExtra(NO_CALORIES, Double.parseDouble(formatter.format(totalCaloriesActivity)));
        sendBroadcast(intent);
    }
    private double getUserWeight(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(userLogged, type);
        return user.getWeight();
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

    public void addActivitySharedPrefs(){
        SharedPreferences sharedPref = this.getSharedPreferences("activityDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonActivity = gson.toJson(activityDataList);
        editor.putString("activity", jsonActivity);
        editor.apply();
    }

}