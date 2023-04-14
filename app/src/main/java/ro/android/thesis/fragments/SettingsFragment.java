package ro.android.thesis.fragments;

import android.content.Context;
import android.content.Intent;
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
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.StepService;

public class SettingsFragment extends Fragment implements AuthenticationObserver {
    Button btnLogOut;
    SyncConfiguration syncConfiguration;
    User mongoUser;
    CalAidApp calAidApp;

    LoadingDialog loadingDialog = new LoadingDialog();


    public SettingsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calAidApp = (CalAidApp) getActivity().getApplication();
        calAidApp.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
        btnLogOut = rootView.findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(view -> {
            logOutUser();
        });

        return rootView;
    }

    //TODO: Stop service when the user is logged out.
    //TODO: display steps from last logout - get step count from service and store it in shared prefs
    private void logOutUser() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        loadingDialog.setCancelable(false);
        loadingDialog.show(getChildFragmentManager(), "loading_screen");
//                            loadingDialog.dismiss();
//        calAidApp.setSyncConfigurationMain(null);
//        calAidApp.setAppUser(null);
//                    Intent i = new Intent(getActivity(), LogInActivity.class);
//                    startActivity(i);
        if (sharedPref.getString("user", null) == null) {
            Log.d("Realm", "Cleared Shared prefs");
            calAidApp.getAppUser().logOutAsync(result -> {
                if(result.isSuccess()){
                    Log.d("CALAIDAPP", "User Logged out");
                    Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
                    Log.d("CALAIDAPP", String.valueOf(calAidApp.getSyncConfigurationMain()));
                    calAidApp.setSyncConfigurationMain(null);
                    Log.d("CALAIDAPP", String.valueOf(calAidApp.getSyncConfigurationMain()));
                    if(calAidApp.getAppUser() != null){
                        calAidApp.setAppUser(null);
                        //Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
                    }
                    loadingDialog.dismiss();
                    Intent i = new Intent(getActivity(), LogInActivity.class);
                    startActivity(i);
                }
            });
        }
    }

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
