package ro.android.thesis.fragments;

import android.content.Context;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.domain.AccelerometerData;
import ro.android.thesis.domain.StepCount;
import ro.android.thesis.domain.User;
import ro.android.thesis.utils.KeyboardUtils;

public class ProfileFragment extends Fragment {
    Realm realm;
    //EditText etUpdateEmail;
    EditText etUpdateHeight;
    EditText etUpdateWeight;

    Button btnSaveUpdate;
    Button btnUpdatePassword;
    User user;
    Handler handler = new Handler(Looper.getMainLooper());
    User userCopy;

    SyncConfiguration syncConfiguration;
    io.realm.mongodb.User mongoUser;
    CalAidApp calAidApp;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        //etUpdateEmail = rootView.findViewById(R.id.etUpdateEmail);
        etUpdateHeight = rootView.findViewById(R.id.etUpdateHeight);

        etUpdateWeight = rootView.findViewById(R.id.etUpdateWeight);
        btnSaveUpdate = rootView.findViewById(R.id.btnSaveUpdate);
        btnUpdatePassword = rootView.findViewById(R.id.btnUpdatePassword);
        KeyboardUtils.setupUI(rootView, this.getActivity());

        loadingDialog.setCancelable(false);
        loadingDialog.show(getChildFragmentManager(), "loading_screen");
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


        return rootView;
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
}
