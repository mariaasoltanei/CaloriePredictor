package ro.android.thesis.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import io.realm.mongodb.App;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.ActivityItemAdapter;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.LogInActivity;
import ro.android.thesis.R;
import ro.android.thesis.StepServiceViewModel;
import ro.android.thesis.broadcasts.ActivityReceiver;
import ro.android.thesis.broadcasts.StepCountReceiver;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.domain.ActivityData;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.ActivityService;
import ro.android.thesis.services.GyroscopeService;
import ro.android.thesis.services.StepService;

public class SettingsFragment extends Fragment implements AuthenticationObserver {
    private static final String TAG = "SettingsFragment";
    SyncConfiguration syncConfiguration;
    private RecyclerView recyclerView;
    private ActivityItemAdapter activityItemAdapter;
    private List<ActivityData> activityDataList;
    User mongoUser;
    CalAidApp calAidApp;
    private ActivityReceiver activityReceiver;


    public SettingsFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        calAidApp = (CalAidApp) getActivity().getApplication();
        calAidApp.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_settings, container, false);

    }

    //TODO: display steps from last logout - get step count from service and store it in shared prefs

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        recyclerView = view.findViewById(R.id.recyclerViewActivity);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        activityDataList = new ArrayList<>();
        activityItemAdapter = new ActivityItemAdapter(activityDataList);
        recyclerView.setAdapter(activityItemAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        activityReceiver = new ActivityReceiver(activityItemAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ActivityService.ACTIVITY_ACTION);
        getActivity().registerReceiver(activityReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        getActivity().unregisterReceiver(activityReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroy");
        calAidApp.removeObserver(this);
    }

    @Override
    public void update(SyncConfiguration syncConfiguration, User user) {
        this.syncConfiguration = syncConfiguration;
        this.mongoUser = user;
    }
}