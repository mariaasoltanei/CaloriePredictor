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

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.realm.mongodb.App;
import io.realm.mongodb.User;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.LogInActivity;
import ro.android.thesis.R;
import ro.android.thesis.dialogs.LoadingDialog;

public class SettingsFragment extends Fragment {
    Button btnLogOut;


    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        btnLogOut = rootView.findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(view -> {
            logOutUser();
            Intent i = new Intent(getActivity(), LogInActivity.class);
            startActivity(i);
        });

        return rootView;
    }
    private void logOutUser(){
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        if (sharedPref.getString("user", null) == null) {
            Log.d("Realm", "Cleared Shared prefs");
        }
        CalAidApp.getApp().currentUser().logOutAsync(result -> {
            if(CalAidApp.getApp().currentUser() == null){
                Log.d("Realm", "User Logged out");
                CalAidApp.setSyncConfigurationMain(null);
            }
            //Log.d("Realm",CalAidApp.getApp().currentUser().getId());
        });

    }
}
