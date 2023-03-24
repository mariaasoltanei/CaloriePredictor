package ro.android.thesis.fragments;

import static android.content.Context.SENSOR_SERVICE;

import android.Manifest;
import android.content.Context;
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

import ro.android.thesis.MainActivity;
import ro.android.thesis.R;
import ro.android.thesis.domain.User;


public class DashboardFragment extends Fragment implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorStepCounter;
    private Sensor sensorStepDetector;

    User user;

    private int numStepsTaken = 0;
    private int numStepsReported = 0;
    private int numSteptDetector = 0;
    private int statusProgressSteps = 0;

    private Handler progressBarbHandler = new Handler();

    TextView countSteps;
    TextView accelerometerData;
    TextView userName;
    TextView percentageGoal;
    CircularProgressIndicator circularProgressIndicator;
    public DashboardFragment(){

    }
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        if(!stepSensorsAvailable()){
            showToast("Sensors not available for this device!");
        }
        countSteps = rootView.findViewById(R.id.tvDailySteps);
        accelerometerData = rootView.findViewById(R.id.tvAccelerometer);
        userName = rootView.findViewById(R.id.tvUserName);
        circularProgressIndicator = rootView.findViewById(R.id.progressBarSteps);
        percentageGoal = rootView.findViewById(R.id.tvPercentageGoal);
        circularProgressIndicator.setMax(100);

        sensorManager = (SensorManager) this.getContext().getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        registerSensorListeners();
        loadSharePrefsData();
        startProgressBarThread();
        return rootView;
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

    private void registerSensorListeners(){
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorStepCounter, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
            if(numStepsReported < 1){
                numStepsReported = (int) sensorEvent.values[0];
                Log.d("STEP COUNTER", String.valueOf(sensorEvent.values[0]));
            }
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            numSteptDetector++;
            countSteps.setText(String.valueOf(numSteptDetector) + "/10.000 steps");
        }
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            String x = String.format("%.02f", sensorEvent.values[0]);
            String y = String.format("%.02f", sensorEvent.values[1]);
            String z = String.format("%.02f", sensorEvent.values[2]);
            accelerometerData.setText(x + " " + y + " " + z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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
        double noSteps = Double.parseDouble(String.valueOf(countSteps.getText().charAt(0)));
        if(noSteps == 0) {
            return 1 * 0;
        }
        return (double) (noSteps*0.10);

    }
    public void startProgressBarThread(){
        progressBarbHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                double i  = convertStepsToProgress();
                int percentage;
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

}