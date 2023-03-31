package ro.android.thesis.fragments;

import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Calendar;

import ro.android.thesis.R;
import ro.android.thesis.broadcasts.StepCountReceiver;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.StepService;


public class DashboardFragment extends Fragment {
    private SensorManager sensorManager;
    private Handler resetNumStepsHandler;
    private Runnable resetNumStepsRunnable;

    User user;
    private StepCountReceiver stepCountReceiver;

    private Handler progressBarbHandler = new Handler();

    TextView countSteps;
    TextView userName;
    TextView percentageGoal;
    CircularProgressIndicator circularProgressIndicator;
    public DashboardFragment(){

    }
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startProgressBarThread();
        if (ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), new String[] {Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        if(!stepSensorsAvailable()){
            showToast("Sensors not available for this device!");
        }
        countSteps = rootView.findViewById(R.id.tvDailySteps);
        userName = rootView.findViewById(R.id.tvUserName);
        circularProgressIndicator = rootView.findViewById(R.id.progressBarSteps);
        percentageGoal = rootView.findViewById(R.id.tvPercentageGoal);
        circularProgressIndicator.setMax(100);

        sensorManager = (SensorManager) this.getContext().getSystemService(SENSOR_SERVICE);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                StepService.StepCountBinder binder = (StepService.StepCountBinder) iBinder;
                StepService service = binder.getService();
                int stepCount = service.getStepCount();
                Log.d("STEP COUNTER", "DashboardFragment/" + stepCount);
                countSteps.setText(String.valueOf(stepCount));
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
//        resetNumStepsHandler = new Handler();
//        resetNumStepsRunnable = () -> {
//            countSteps.setText("");
//            resetNumStepsHandler.postDelayed(resetNumStepsRunnable, getTimeUntilMidnight());
//        };

        //resetNumStepsHandler.postDelayed(resetNumStepsRunnable, getTimeUntilMidnight());

        Intent serviceIntent = new Intent(getActivity(), StepService.class);
        getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        //registerSensorListeners();
        loadSharePrefsData();
        //startProgressBarThread();
        return rootView;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, start using the step counter sensor
                } else {
                    // Permission denied, handle accordingly
                }
            }
            return;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        stepCountReceiver = new StepCountReceiver(countSteps);
        IntentFilter filter = new IntentFilter();
        filter.addAction(StepService.STEP_COUNT_ACTION);
        getActivity().registerReceiver(stepCountReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(stepCountReceiver);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        resetNumStepsHandler.removeCallbacks(resetNumStepsRunnable);
    }
    public boolean stepSensorsAvailable(){
        PackageManager packageManager = this.getContext().getPackageManager();
        int apiVersion = Build.VERSION.SDK_INT;
        return apiVersion >= 19 && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
    }
    public void showToast(final String message)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSharePrefsData(){
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        user = gson.fromJson(userLogged, type);

        Log.d("SHARED PREFS TEST", String.valueOf(user));
        if(user != null) {
            userName.setText("Hi, "+user.getFirstName() +"!");
        }
    }
    public double convertStepsToProgress(){
        double noSteps = Double.parseDouble(String.valueOf(countSteps.getText()));
        if(noSteps == 0) {
            return 1 * 0;
        }
        return (double) (noSteps*0.01);

    }
    public void startProgressBarThread(){
        progressBarbHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                double i  = convertStepsToProgress();
                int percentage;
                if(i > 100){
                    circularProgressIndicator.setProgress(100);
                    percentageGoal.setText("Contratulations! You reached your step goal");
                }
                if (i <= 100) { //maybe its i<=10000
                    circularProgressIndicator.setProgress((int) i);
                    percentageGoal.setText("You have walked " + (int) i +"% of today's goal.");
                    i++;
                    progressBarbHandler.postDelayed(this, 200);
                } else {
                    progressBarbHandler.removeCallbacks(this);
                }
            }
        }, 200);
    }
    private long getTimeUntilMidnight() {
        Calendar midnightCalendar = Calendar.getInstance();
        midnightCalendar.set(Calendar.HOUR_OF_DAY, 24);
        midnightCalendar.set(Calendar.MINUTE, 0);
        midnightCalendar.set(Calendar.SECOND, 0);
        midnightCalendar.set(Calendar.MILLISECOND, 0);

        long currentTime = System.currentTimeMillis();
        long midnightTime = midnightCalendar.getTimeInMillis();

        if (midnightTime < currentTime) {
            midnightCalendar.add(Calendar.DAY_OF_MONTH, 1);
            midnightTime = midnightCalendar.getTimeInMillis();
        }

        return midnightTime - currentTime;
    }

}