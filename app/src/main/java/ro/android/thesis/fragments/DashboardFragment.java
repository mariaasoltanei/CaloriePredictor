package ro.android.thesis.fragments;

import android.Manifest;
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

import java.io.IOException;
import java.lang.reflect.Type;

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
import ro.android.thesis.broadcasts.StepCountReceiver;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.StepService;


public class DashboardFragment extends Fragment {
    private final String TAG = "DashboardFragment";
    User user;

    private StepServiceViewModel stepServiceViewModel;
    public StepCountReceiver stepCountReceiver;
    ServiceConnection serviceConnection;

    private Handler progressBarbHandler = new Handler();

    private ActivityResultLauncher<String> requestPermissionLauncher;

    TextView countSteps;
    TextView userName;
    TextView percentageGoal;
    TextView tvNumCalories;
    CircularProgressIndicator circularProgressIndicator;
    StepService.StepCountBinder binder;

    private String url = "http://192.168.0.106:5000/calories/"; //+ CalAidApp.getApp().currentUser().getId();
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
        //setRetainInstance(true);
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
        tvNumCalories = rootView.findViewById(R.id.tvNumCalories);
        connect = rootView.findViewById(R.id.btnTestRequest);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postRequest("hello world", url);

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
        loadSharePrefsData();
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
    private void postRequest(String message, String URL) {
        RequestBody requestBody = buildRequestBody(message);
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d("OkHTTTP", response.body().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
    public void unregisterReceiver() {
        getActivity().unregisterReceiver(stepCountReceiver);
    }

}