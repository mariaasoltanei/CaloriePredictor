package ro.android.thesis.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.LogInActivity;
import ro.android.thesis.R;
import ro.android.thesis.StepServiceViewModel;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.domain.AccelerometerData;
import ro.android.thesis.domain.StepCount;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.ActivityService;
import ro.android.thesis.services.GyroscopeService;
import ro.android.thesis.services.StepService;
import ro.android.thesis.utils.KeyboardUtils;

public class ProfileFragment extends Fragment implements AuthenticationObserver {
    Realm realm;
    //EditText etUpdateEmail;
    EditText etUpdateHeight;
    EditText etUpdateWeight;

    Button btnSaveUpdate;
    Button btnLogOut;
    User user;
    Handler handler = new Handler(Looper.getMainLooper());
    User userCopy;

    SyncConfiguration syncConfiguration;
    io.realm.mongodb.User mongoUser;
    CalAidApp calAidApp;
    private StepServiceViewModel stepServiceViewModel;
    ServiceConnection serviceConnection;

    LoadingDialog loadingDialog = new LoadingDialog();

    public ProfileFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calAidApp = (CalAidApp) getActivity().getApplication();
        syncConfiguration = calAidApp.getSyncConfigurationMain();
        calAidApp.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        //etUpdateEmail = rootView.findViewById(R.id.etUpdateEmail);
        etUpdateHeight = rootView.findViewById(R.id.etUpdateHeight);

        etUpdateWeight = rootView.findViewById(R.id.etUpdateWeight);
        btnSaveUpdate = rootView.findViewById(R.id.btnSaveUpdate);
        btnLogOut = rootView.findViewById(R.id.btnLogOut);
        KeyboardUtils.setupUI(rootView, this.getActivity());
//TODO: user should be able to edit activity multiplier
        loadingDialog.setCancelable(false);
        loadingDialog.show(getChildFragmentManager(), "loading_screen");
        //loadingDialog.dismiss();
        new Thread(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(syncConfiguration);
                final User user = realm.where(User.class).equalTo("email", getUserEmail()).findFirst();
//                RealmResults<AccelerometerData> results = realm.where(AccelerometerData.class).findAll();
//                realm.executeTransaction(realm -> results.deleteAllFromRealm());
//                RealmResults<StepCount> resultsSteps = realm.where(StepCount.class).findAll();
//                realm.executeTransaction(realm -> resultsSteps.deleteAllFromRealm());
                userCopy = realm.copyFromRealm(user);
                Log.d("Realm", "ProfileFragment/" + userCopy);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        etUpdateHeight.setText(String.valueOf((int) userCopy.getHeight()));
                        etUpdateWeight.setText(String.valueOf((int) userCopy.getWeight()));
                        loadingDialog.dismiss();
                    }
                });
                realm.close();
            }
        }).start();
        btnSaveUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingDialog.setCancelable(false);
                loadingDialog.show(getChildFragmentManager(), "loading_screen");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        realm = Realm.getInstance(syncConfiguration);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                //userCopy.setEmail(etUpdateEmail.getText().toString());
                                userCopy.setHeight(Integer.parseInt(etUpdateHeight.getText().toString()));
                                userCopy.setWeight(Integer.parseInt(etUpdateWeight.getText().toString()));
                                realm.insertOrUpdate(userCopy);
                            }
                        });
                        showToast("Data saved!");
                        addUserToSharedPreferences(userCopy);
                        loadingDialog.dismiss();
                        realm.close();
                    }
                }).start();


            }
        });
        stepServiceViewModel = new ViewModelProvider(requireActivity()).get(StepServiceViewModel.class);
        serviceConnection = stepServiceViewModel.getStepServiceConnection();
        Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOutUser();
            }
        });


        return rootView;
    }
    public void onDestroyView() {
        super.onDestroyView();
        calAidApp.removeObserver(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

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

    private String getUserEmail() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(userLogged, type);
        return user.getEmail();
    }

    private void addUserToSharedPreferences(User user) {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        editor.putString("user", jsonUser);
        //editor.clear();
        editor.apply();
    }
    private void logOutUser() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
        deleteActivitiesSharedPrefs();
        loadingDialog.setCancelable(false);
        loadingDialog.show(getChildFragmentManager(), "loading_screen");
//        loadingDialog.dismiss();
//        calAidApp.setSyncConfigurationMain(null);
//        calAidApp.setAppUser(null);
//        Intent i = new Intent(getActivity(), LogInActivity.class);
//        startActivity(i);
        if (sharedPref.getString("user", null) == null) {
            Log.d("Realm", "Cleared Shared prefs");
            calAidApp.getAppUser().logOutAsync(result -> {
                if(result.isSuccess()){
                    Log.d("CALAIDAPP", "User Logged out");
                    Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
                    Log.d("CALAIDAPP", String.valueOf(calAidApp.getSyncConfigurationMain()));
                    calAidApp.setSyncConfigurationMain(null);
                    Log.d("CALAIDAPP", String.valueOf(calAidApp.getSyncConfigurationMain()));

                    Intent stopAccServiceIntent = new Intent(this.getActivity().getApplicationContext(), AccelerometerService.class);
                    stopAccServiceIntent.setAction("stopAccService");
                    this.getActivity().getApplicationContext().stopService(stopAccServiceIntent);
                    Intent stopGyroServiceIntent = new Intent(this.getActivity().getApplicationContext(), GyroscopeService.class);
                    stopGyroServiceIntent.setAction("stopGyroService");
                    this.getActivity().getApplicationContext().stopService(stopGyroServiceIntent);

                    if(stepServiceViewModel.isServiceBound() && stepServiceViewModel.getStepServiceConnection() != null){

                        Log.d("CALAIDAPP-SERVICE CONN", stepServiceViewModel.getStepServiceConnection().toString());
                        getContext().unbindService(serviceConnection);
                        stepServiceViewModel.setServiceBound(false);
                        stepServiceViewModel.setStepServiceConnection(null);
                        Intent stopStepService = new Intent(this.getActivity().getApplicationContext(), StepService.class);
                        stopStepService.setAction("stopStepService");
                        this.getActivity().getApplicationContext().stopService(stopStepService);

                        Intent stopAcctivityServiceIntent = new Intent(this.getActivity().getApplicationContext(), ActivityService.class);
                        stopAcctivityServiceIntent.setAction("stopActivityService");
                        this.getActivity().getApplicationContext().stopService(stopAcctivityServiceIntent);
                    }


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
    public void deleteActivitiesSharedPrefs(){
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("activityDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();
    }
    @Override
    public void update(SyncConfiguration syncConfiguration, io.realm.mongodb.User user) {
        this.syncConfiguration = syncConfiguration;
        this.mongoUser = user;
    }
}