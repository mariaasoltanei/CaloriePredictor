package ro.android.thesis.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
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
import ro.android.thesis.ActivityServiceViewModel;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.broadcasts.ActivityReceiver;
import ro.android.thesis.domain.ActivityData;
import ro.android.thesis.services.ActivityService;


public class SettingsFragment extends Fragment implements AuthenticationObserver {
    private static final String TAG = "SettingsFragment";
    SyncConfiguration syncConfiguration;
    private RecyclerView recyclerView;
    private ActivityItemAdapter activityItemAdapter;
    private List<ActivityData> activityDataList;
    ServiceConnection activityServiceConnection;
    User mongoUser;
    CalAidApp calAidApp;
    private ActivityReceiver activityReceiver;
    ActivityService.ActivityBinder activityBinder;
    private ActivityServiceViewModel activityServiceViewModel;


    public SettingsFragment() {
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
        activityServiceViewModel = new ViewModelProvider(requireActivity()).get(ActivityServiceViewModel.class);
        activityServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                activityBinder = (ActivityService.ActivityBinder) iBinder;
                ActivityService service = activityBinder.getService();
//                int stepCount = service.getStepCount();
                Log.d(TAG, "SERVICE CONN ACTIVE" );
//                countSteps.setText(String.valueOf(stepCount));
                activityServiceViewModel.setServiceBound(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                activityBinder = null;
                activityServiceViewModel.setServiceBound(false);
            }
        };

        activityServiceViewModel.setActivityServiceConnection(activityServiceConnection);

        Intent serviceIntent = new Intent(getActivity(), ActivityService.class);
        getActivity().bindService(serviceIntent, activityServiceConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        return inflater.inflate(R.layout.fragment_settings, container, false);

    }
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
