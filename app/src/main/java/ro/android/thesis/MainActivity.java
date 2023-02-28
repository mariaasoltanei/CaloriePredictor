package ro.android.thesis;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private Context context;

    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorStepCounter;
    private Sensor sensorStepDetector;

    private int numStepsTaken = 0;
    private int numStepsReported = 0;
    private int numSteptDetector = 0;

    TextView countSteps;
    TextView accelerometerData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.imgIconMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setItemIconTintList(null);

        NavController navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(navigationView, navController);

        if(!stepSensorsAvailable()){
            showToast("Sensors not available for this device!");
            return;
        }
        countSteps = findViewById(R.id.tvSteps);
        accelerometerData = findViewById(R.id.tvAccelerometer);

        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        registerSensorListeners();

    }
    public boolean stepSensorsAvailable(){
        PackageManager packageManager = context.getPackageManager();
        int apiVersion = Build.VERSION.SDK_INT;
        return apiVersion >= 19 && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)
                                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
                                && packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
    }
    public void showToast(final String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
}
