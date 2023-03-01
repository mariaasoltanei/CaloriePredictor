package ro.android.thesis;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;


public class DashboardFragment extends Fragment implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorStepCounter;
    private Sensor sensorStepDetector;

    User user;

    private int numStepsTaken = 0;
    private int numStepsReported = 0;
    private int numSteptDetector = 0;

    TextView countSteps;
    TextView accelerometerData;
    TextView userName;
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
            //return;
        }
        countSteps = rootView.findViewById(R.id.tvSteps);
        accelerometerData = rootView.findViewById(R.id.tvAccelerometer);
        userName = rootView.findViewById(R.id.tvUserName);

        sensorManager = (SensorManager) this.getContext().getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        registerSensorListeners();
        loadSharePrefsData();
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
            countSteps.setText(String.valueOf(numSteptDetector));
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
}