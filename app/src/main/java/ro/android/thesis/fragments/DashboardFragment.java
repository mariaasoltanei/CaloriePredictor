package ro.android.thesis.fragments;

import android.Manifest;
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
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.StepServiceViewModel;
import ro.android.thesis.broadcasts.StepCountReceiver;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.StepService;
import ro.android.thesis.utils.FitnessCalculations;


public class DashboardFragment extends Fragment {
    private final String TAG = "DashboardFragment";
    public StepCountReceiver stepCountReceiver;
    User user;
    ServiceConnection serviceConnection;
    TextView countSteps;
    TextView userName;
    TextView percentageGoal;
    TextView tvNumCalories;
    TextView tvCaloriesConsumed;
    TextView tvSpeed;
    Button btnUpdateCalories;
    CircularProgressIndicator circularProgressIndicator;
    StepService.StepCountBinder binder;
    private double calories;
    private String activity;
    private long startTime = 0;
    private int noStepsStart;
    private double updatedCalories = 0;
    private boolean isThreadRunning = false;
    private Handler requestHandler;

    private Runnable requestRunnable;
    private StepServiceViewModel stepServiceViewModel;
    private final Handler progressBarbHandler = new Handler();
    private ActivityResultLauncher<String> requestPermissionLauncher;
    //TODO: add wait time
    private final String url = "http://192.168.0.102:5000/activityMultiplier/" + CalAidApp.getApp().currentUser().getId();
    private String postBodyString;
    private MediaType mediaType;
    private RequestBody requestBody;

    public DashboardFragment() {

    }

    public void setUpdatedCalories(double updatedCalories) {
        this.updatedCalories = updatedCalories;
    }

    public int getNoSteps() {
        return noStepsStart;
    }

    public void setNoSteps(int noStepsStart) {
        this.noStepsStart = noStepsStart;
    }

    public double getCalories() {
        return calories;
    }

    public void setCalories(double calories) {
        this.calories = calories;
    }

    public StepCountReceiver getStepCountReceiver() {
        return stepCountReceiver;
    }

    public ServiceConnection getServiceConnection() {
        return serviceConnection;
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
        //TODO: calculate target Calories, hardcode timestamp in request
        tvNumCalories = rootView.findViewById(R.id.tvNumCalories);
        tvCaloriesConsumed = rootView.findViewById(R.id.tvCaloriesConsumed);
        tvSpeed = rootView.findViewById(R.id.tvSpeed);
        btnUpdateCalories = rootView.findViewById(R.id.btnUpdateCalories);
        btnUpdateCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getTDEE();
            }
        });
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = (StepService.StepCountBinder) iBinder;
                StepService service = binder.getService();
                int stepCount = service.getStepCount();
                double speed = service.getSpeed();
                double calories = service.getCalories();
                Log.d("STEP COUNTER", "DashboardFragment/" + stepCount);
                countSteps.setText(String.valueOf(stepCount));
                tvSpeed.setText(String.format("%,.2f", speed));
                tvCaloriesConsumed.setText(String.format("%,.2f", calories));
                setNoSteps(stepCount);
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
        tvNumCalories.setText(String.valueOf(FitnessCalculations.calculateBMR(getUserSharedPrefs())));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        stepCountReceiver = new StepCountReceiver(countSteps, tvSpeed, tvCaloriesConsumed);
        IntentFilter filter = new IntentFilter();
        filter.addAction(StepService.STEP_COUNT_ACTION);
        filter.addAction(StepService.SPEED_ACTION);
        getActivity().registerReceiver(stepCountReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        requestHandler.removeCallbacks(requestRunnable);
        getActivity().unregisterReceiver(stepCountReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
//        handler.removeCallbacks(runnable);
//        timerHandler.removeCallbacks(runnable);
        //getActivity().unregisterReceiver(stepCountReceiver);
//        resetNumStepsHandler.removeCallbacks(resetNumStepsRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
//        handler.removeCallbacks(runnable);
//        timerHandler.removeCallbacks(runnable);

    }

    private void getTDEE() {
        Log.d("OkHTTTPpost", "Starting post req");
        requestHandler = new Handler(Looper.getMainLooper());
        requestRunnable = new Runnable() {
            @Override
            public void run() {
                postRequest(url);

            }
        };
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Simulate some delay
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                requestHandler.post(requestRunnable);
            }
        }).start();


    }

    //    private RequestBody buildRequestBody(String msg) {
//        postBodyString = msg;
//        mediaType = MediaType.parse("application/json; charset=utf-8");
//        requestBody = RequestBody.create(jsonObject.toString(), mediaType);
//        return requestBody;
//    }
    private RequestBody buildRequestBody(JSONObject jsonObject) {
        String jsonStr = jsonObject.toString();
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(jsonStr, mediaType);
        return requestBody;
    }

    private void postRequest(String URL) {
        try {
            JSONObject jsonObject = new JSONObject();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            Date currentDate = new Date();
            double currentBMR = Double.parseDouble(tvNumCalories.getText().toString());
            String currentDateString = dateFormat.format(currentDate);
            jsonObject.put("timestamp", "2023-05-28 03:41:30.733000");
            jsonObject.put("currentBMR", currentBMR);
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
                        double caloriesRequest = jsonObject.getDouble("TDEE");
                        setUpdatedCalories(caloriesRequest);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvNumCalories.setText(String.valueOf(caloriesRequest));
                            }
                        });
                        Log.d("OkHTTTPpost", String.valueOf(caloriesRequest));


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


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
                try {
                    //Log.d("OkHTTTP", response.body().string());
                    JSONObject jsonObject = new JSONObject(response.body().string());
//                    calories = jsonObject.getDouble("calories");
//                    speed = jsonObject.getDouble("speed");
//                    //duration = jsonObject.getDouble("activityDurationMins");
//                    setCalories(calories);
//                    setSpeed(speed);
//
//                    Log.d("OkHTTTP", String.valueOf(calories));
//                    Log.d("OkHTTTP", String.valueOf(speed));
                    //Log.d("OkHTTTP", String.valueOf(duration));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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
            return 0;
        }
        return noSteps * 0.01;

    }
    //TODO: notification foreground service

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

    private double getUserWeight() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(userLogged, type);
        return user.getWeight();
    }

    private User getUserSharedPrefs() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(userLogged, type);
        return user;
    }

//    private void startThread() {
//        if (!isThreadRunning) {
//            isThreadRunning = true;
//            startTime = System.currentTimeMillis();
////            handler = new Handler(Looper.getMainLooper());
//            runnable = new Runnable() {
//                @Override
//                public void run() {
//                    if (isThreadRunning) {
//                        postRequest(url);
//                        handler.postDelayed(this, 120004);
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
////                                long elapsedTime = System.currentTimeMillis() - startTime;
////                                int seconds = (int) (elapsedTime / 1000) % 60;
////                                int minutes = (int) ((elapsedTime / (1000*60)) % 60);
////                                String timeString = String.format("%02d:%02d", minutes, seconds);
////                                tvDuration.setText(String.valueOf(timeString));
////                                totalNumberCalories += calories;
////                                tvCaloriesConsumed.setText(String.format("%,.2f",totalNumberCalories));
////                                tvSpeed.setText(String.format("%,.2f", speed));
//                            }
//                        });
//                    }
//                }
//            };
//            handler.postDelayed(runnable, 120004);
//            //timerHandler.postDelayed(runnable, 1000);
//        }
//    }

//    private void stopThread() {
//        if (isThreadRunning) {
//            // totalNumberCalories = 0;
//            isThreadRunning = false;
//            handler.removeCallbacks(runnable);
//            timerHandler.removeCallbacks(runnable);
//        }
//    }

    public void unregisterReceiver() {
        getActivity().unregisterReceiver(stepCountReceiver);
    }

}