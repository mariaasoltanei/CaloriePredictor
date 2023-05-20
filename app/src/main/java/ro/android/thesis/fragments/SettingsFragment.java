package ro.android.thesis.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.LogInActivity;
import ro.android.thesis.R;
import ro.android.thesis.StepServiceViewModel;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.GyroscopeService;
import ro.android.thesis.services.StepService;

public class SettingsFragment extends Fragment implements AuthenticationObserver {
    Button btnLogOut;
    SyncConfiguration syncConfiguration;
    User mongoUser;
    CalAidApp calAidApp;

    private StepServiceViewModel stepServiceViewModel;
    ServiceConnection serviceConnection;

    LoadingDialog loadingDialog = new LoadingDialog();
    DashboardFragment dashboardFragment;


    public SettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        calAidApp = (CalAidApp) getActivity().getApplication();
        calAidApp.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        //dashboardFragment = (DashboardFragment) getParentFragmentManager().findFragmentById(R.id.dashboardFragment);
        stepServiceViewModel = new ViewModelProvider(requireActivity()).get(StepServiceViewModel.class);
        serviceConnection = stepServiceViewModel.getStepServiceConnection();
        Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
        btnLogOut = rootView.findViewById(R.id.btnTest);
        btnLogOut.setOnClickListener(view -> {
           // logOutUser();
        });

        return rootView;
    }

    //TODO: display steps from last logout - get step count from service and store it in shared prefs


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        calAidApp.removeObserver(this);
    }

    @Override
    public void update(SyncConfiguration syncConfiguration, User user) {
        this.syncConfiguration = syncConfiguration;
        this.mongoUser = user;
    }
}
