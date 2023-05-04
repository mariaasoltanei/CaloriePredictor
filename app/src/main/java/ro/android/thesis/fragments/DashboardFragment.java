package ro.android.thesis.fragments;

import static android.content.Context.ALARM_SERVICE;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.StepServiceViewModel;
import ro.android.thesis.broadcasts.NotificationReceiver;
import ro.android.thesis.broadcasts.StepCountReceiver;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.StepService;


public class DashboardFragment extends Fragment {
    private final String TAG = "DashboardFragment";
    User user;
    private boolean isThreadRunning = false;
    private Handler handler;
    private Handler requestHandler;
    private Runnable runnable;
    private StepServiceViewModel stepServiceViewModel;
    public StepCountReceiver stepCountReceiver;
    ServiceConnection serviceConnection;

    private Handler progressBarbHandler = new Handler();

    private ActivityResultLauncher<String> requestPermissionLauncher;

    TextView countSteps;
    TextView userName;
    TextView percentageGoal;
    TextView tvNumCalories;
    TextView tvCaloriesConsumed;
    TextView tvSpeed;
    TextView tvDuration;
    CircularProgressIndicator circularProgressIndicator;
    StepService.StepCountBinder binder;

    private String url = "http://192.168.0.100:5000/calories/" + CalAidApp.getApp().currentUser().getId();
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;
    private Button connect;

    public StepCountReceiver getStepCountReceiver() {
        return stepCountReceiver;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public DashboardFragment() {

    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Permission is granted
                // Do something here
            } else {
                // Permission is not granted
                // Do something here
            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Log.d(TAG, "onCreateView");
        Log.d(TAG, getActivity().toString());

        if (!stepSensorsAvailable()) {
            showToast("Sensors not available for this device!");
        }
        stepServiceViewModel = new ViewModelProvider(requireActivity()).get(StepServiceViewModel.class);
        countSteps = rootView.findViewById(R.id.tvDailySteps);
        startProgressBarThread();
        userName = rootView.findViewById(R.id.tvUserName);
        circularProgressIndicator = rootView.findViewById(R.id.progressBarSteps);
        percentageGoal = rootView.findViewById(R.id.tvPercentageGoal);
        circularProgressIndicator.setMax(100);
        //TODO: calculate target Calories
        tvNumCalories = rootView.findViewById(R.id.tvNumCalories);
        tvCaloriesConsumed = rootView.findViewById(R.id.tvCaloriesConsumed);
        tvSpeed = rootView.findViewById(R.id.tvSpeed);
        tvDuration = rootView.findViewById(R.id.tvDuration);
        connect = rootView.findViewById(R.id.btnTestRequest);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getRequest(url);
                if (!isThreadRunning) {
                    startThread();
                    connect.setText("End");
                }
                else {
                    stopThread();
                    connect.setText("Start");
                }
            }
        });
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (StepService.StepCountBinder) iBinder;
                StepService service = binder.getService();
                int stepCount = service.getStepCount();
                Log.d("STEP COUNTER", "DashboardFragment/" + stepCount);
                countSteps.setText(String.valueOf(stepCount));
                if(stepCount == 100){
                    sendNotification("Congrats! First 100 steps.");
                }
                if(stepCount == 1000 || stepCount == 5000 || stepCount == 8000){
                    String notificationText = "Keep up the good work! You have reached " + stepCount + "steps.";
                    sendNotification(notificationText);
                }
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                if(stepCount < 100 && hourOfDay == 15){
                    sendNotification("Time for some exercise!");
                }
                stepServiceViewModel.setServiceBound(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                binder = null;
                stepServiceViewModel.setServiceBound(false);
            }
        };
        stepServiceViewModel.setStepServiceConnection(serviceConnection);

        Intent serviceIntent = new Intent(getActivity(), StepService.class);
        getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
        //sendNotification();
        loadSharePrefsData();
        tvNumCalories.setText(String.valueOf(calculateCalories()));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        stepCountReceiver = new StepCountReceiver(countSteps);
        IntentFilter filter = new IntentFilter();
        filter.addAction(StepService.STEP_COUNT_ACTION);
        getActivity().registerReceiver(stepCountReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        getActivity().unregisterReceiver(stepCountReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        //getActivity().unregisterReceiver(stepCountReceiver);
//        resetNumStepsHandler.removeCallbacks(resetNumStepsRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    private RequestBody buildRequestBody(String msg) {
        postBodyString = msg;
        mediaType = MediaType.parse("text/plain");
        requestBody = RequestBody.create(postBodyString, mediaType);
        return requestBody;
    }
    private void getRequest(String URL) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request
                .Builder()
                .get()
                .url(URL)
                .build();
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
                requestHandler = new Handler(Looper.getMainLooper());

                new Thread(new Runnable() {
                    double calories;
                    double speed;
                    double duration;
                    private long startTime = 0;
                    String timeString;
                    @Override
                    public void run() {
                        try {
                            //Log.d("OkHTTTP", response.body().string());
                            startTime = System.currentTimeMillis();
                            long elapsedTime = System.currentTimeMillis() - startTime;
                            int seconds = (int) (elapsedTime / 1000) % 60;
                            int minutes = (int) ((elapsedTime / (1000*60)) % 60);
                            timeString = String.format("%02d:%02d", minutes, seconds);
                            Log.d(TAG, "Timer running: " + timeString);
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            calories = jsonObject.getDouble("calories");
                            speed = jsonObject.getDouble("speed");
                            duration = jsonObject.getDouble("activityDurationMins");

                            Log.d("OkHTTTP", String.valueOf(calories));
                            Log.d("OkHTTTP", String.valueOf(speed));
                            Log.d("OkHTTTP", String.valueOf(duration));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        requestHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvCaloriesConsumed.setText(String.valueOf(calories));
                                tvSpeed.setText(String.format("%,.2f", speed));
                                tvDuration.setText(String.valueOf(timeString));
                            }
                        });
                    }
                }).start();
            }
        });
    }

    public boolean stepSensorsAvailable() {
        PackageManager packageManager = this.getContext().getPackageManager();
        int apiVersion = Build.VERSION.SDK_INT;
        return apiVersion >= 19 && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
    }

    public void showToast(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSharePrefsData() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        user = gson.fromJson(userLogged, type);

        Log.d("SHARED PREFS TEST", String.valueOf(user));
        if (user != null) {
            userName.setText("Hi, " + user.getFirstName() + "!");
        }
    }

    public double convertStepsToProgress() {
        double noSteps = Double.parseDouble(String.valueOf(countSteps.getText()));
        if (noSteps == 0) {
            return 1 * 0;
        }
        return (double) (noSteps * 0.01);

    }
    private void sendNotification(String content){
        Log.d("CALAID", "Notification function");
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), NotificationReceiver.class);
        intent.putExtra("content", content);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +100, AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void startProgressBarThread() {
        countSteps.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                progressBarbHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        double i = convertStepsToProgress();
                        int percentage;
                        if (i > 100) {
                            circularProgressIndicator.setProgress(100);
                            percentageGoal.setText("Contratulations! You reached your step goal");
                        }
                        if (i <= 100) {
                            circularProgressIndicator.setProgress((int) i);
                            percentageGoal.setText("You have walked " + (int) i + "% of today's goal.");
                            i++;
                            progressBarbHandler.postDelayed(this, 200);
                        } else {
                            progressBarbHandler.removeCallbacks(this);
                        }
                    }
                }, 200);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }
    private int calculateCalories() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(userLogged, type);
        //TODO: find age difference with the full date
        Calendar calendar = Calendar.getInstance();
        int age = calendar.get(Calendar.YEAR) - Integer.parseInt(user.getBirthDate().substring(user.getBirthDate().length() - 4));
        Log.d("SHARED PREFS TESTAGE", user.getBirthDate().substring(user.getBirthDate().length() - 4));
        Log.d("SHARED PREFS TESTAGE", String.valueOf(age));
        double bmr = 0;
        if(user.getGender() == "Female"){
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * age + 161;
            Log.d("SHARED PREFS TESTAGE", String.valueOf(bmr));
        }
        else {
            bmr = 10 * user.getWeight() + 6.25 * user.getHeight() - 5 * age + 5;
        }
        return (int) (bmr * user.getActivityMultiplier());
    }
    private void startThread() {
        if (!isThreadRunning) {
            isThreadRunning = true;
            handler = new Handler(Looper.getMainLooper());
            runnable = new Runnable() {
                @Override
                public void run() {
                    getRequest(url);
                    Toast.makeText(getActivity(), "Thread running", Toast.LENGTH_SHORT).show();
                    if (isThreadRunning) {
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private void stopThread() {
        if (isThreadRunning) {
            isThreadRunning = false;
            handler.removeCallbacks(runnable);
        }
    }
    public void unregisterReceiver() {
        getActivity().unregisterReceiver(stepCountReceiver);
    }

}