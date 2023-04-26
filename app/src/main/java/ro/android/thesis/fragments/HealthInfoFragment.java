package ro.android.thesis.fragments;

import static java.lang.Double.parseDouble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.bson.types.ObjectId;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.AuthenticationObserver;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.MainActivity;
import ro.android.thesis.R;
import ro.android.thesis.dialogs.ErrorDialog;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.StepService;
import ro.android.thesis.utils.KeyboardUtils;

public class HealthInfoFragment extends Fragment implements AuthenticationObserver {
    private static final String TAG = "CALAIDAPP";
    Button btnSignUp;
    EditText etSignUpHeight;
    EditText etSignUpWeight;
    Spinner spinSignUpGender;
    Spinner spinSignUpActivity;
    LoadingDialog loadingDialog = new LoadingDialog();
    ErrorDialog errorDialog;
    io.realm.mongodb.User mongoUser;
    Handler handler;
    Runnable runnable;
    private SyncConfiguration syncConfiguration;
    private CalAidApp calAidApp;
    Realm insertRealm;

    public HealthInfoFragment() {
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        calAidApp = (CalAidApp) getActivity().getApplication();
        calAidApp.addObserver(this);
        /**MUST BE NULL*/
        Log.d("CALAIDAPP-SigOnCreate", String.valueOf(calAidApp.getAppUser()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.health_info, container, false);
        String name = getArguments().getString("name");
        String email = getArguments().getString("email");
        String password = getArguments().getString("password");
        String birthDate = getArguments().getString("birthDate");

        etSignUpHeight = rootView.findViewById(R.id.etSignupHeight);
        etSignUpWeight = rootView.findViewById(R.id.etSignupWeight);


        spinSignUpGender = rootView.findViewById(R.id.spinSignUpGender);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.genders_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSignUpGender.setAdapter(spinnerAdapter);

        spinSignUpActivity = rootView.findViewById(R.id.spinSignUpActivity);
        ArrayAdapter<CharSequence> spinnerActivityAdapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.activity_multipliers, android.R.layout.simple_spinner_item);
        spinnerActivityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSignUpActivity.setAdapter(spinnerActivityAdapter);

        //TODO: limit input for weight and height
        KeyboardUtils.setupUI(rootView, this.getActivity());
        btnSignUp = rootView.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(view -> {
            User newUser = new User(new ObjectId(), name, email, password, birthDate,
                    parseDouble(etSignUpHeight.getText().toString()),
                    Double.parseDouble(etSignUpWeight.getText().toString()),
                    String.valueOf(spinSignUpGender.getSelectedItem()),
                    selectActivityMultiplier(spinSignUpActivity.getSelectedItem().toString()));
            Log.d("USER", String.valueOf(newUser));
            signUp(newUser);
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        calAidApp.removeObserver(this);
    }
    //TODO: FUNCTION TO SET UP SPINNER

    private void addUserToSharedPreferences(User user) {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        editor.putString("user", jsonUser);
        //editor.clear();
        editor.apply();
    }

    public double selectActivityMultiplier(String activityLevel) {
        double caloriesPerDay;
        switch (activityLevel) {
            case "Light: exercise 1–3 times/week":
                caloriesPerDay = 1.375;
                break;
            case "Moderate: exercise 4–5 times/week":
                caloriesPerDay = 1.55;
                break;
            case "Active: daily exercise/intense exercise 3–4 times/week":
                caloriesPerDay = 1.725;
                break;
            case "Very Active: intense exercise 6–7 times/week":
                caloriesPerDay = 1.9;
                break;
            case "Extra Active: very intense exercise daily/physical job":
                caloriesPerDay = 2.0;
                break;
            default:
                caloriesPerDay = 1.2;
                break;
        }
        return caloriesPerDay;
    }

    //TODO: see if user already exists
    private void signUp(User user) {
        loadingDialog.setCancelable(false);
        loadingDialog.show(getChildFragmentManager(), "loading_screen");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                CalAidApp.getApp().getEmailPassword().registerUserAsync(user.getEmail(), user.getPassword(), new App.Callback<Void>() {
                    @Override
                    public void onResult(App.Result<Void> resultRegister) {
                        if (resultRegister.isSuccess()) {
                            Log.d(TAG, "User registered");
                            Credentials emailPasswordCredentials = Credentials.emailPassword(user.getEmail(), user.getPassword());
                            CalAidApp.getApp().loginAsync(emailPasswordCredentials, new App.Callback<io.realm.mongodb.User>() {
                                @Override
                                public void onResult(App.Result<io.realm.mongodb.User> resultLogin) {
                                    if (resultLogin.isSuccess()) {
                                        Log.d(TAG, "User logged in");
                                        mongoUser = CalAidApp.getApp().currentUser();
                                        calAidApp.setAppUser(mongoUser);
                                        Log.d("CALAIDAPP-SignUp", String.valueOf(calAidApp.getAppUser()));
                                        syncConfiguration = new SyncConfiguration.Builder(mongoUser)
                                                .waitForInitialRemoteData()
                                                .allowWritesOnUiThread(false)
                                                .initialSubscriptions((realm, subscriptions) -> {
                                                    subscriptions.remove("PasswordSubscription");
                                                    subscriptions.add(Subscription.create("PasswordSubscription",
                                                            realm.where(ro.android.thesis.domain.User.class)
                                                                    .equalTo("password", user.getPassword())));
                                                    subscriptions.remove("AccelerometerData");
                                                    subscriptions.add(Subscription.create("AccelerometerData",
                                                            realm.where(ro.android.thesis.domain.AccelerometerData.class)
                                                                    .equalTo("userId", calAidApp.getAppUser().getId())));
                                                    subscriptions.remove("StepCount");
                                                    subscriptions.add(Subscription.create("StepCount",
                                                            realm.where(ro.android.thesis.domain.StepCount.class)
                                                                    .equalTo("userId", calAidApp.getAppUser().getId())));
                                                })
                                                .build();
                                        calAidApp.setSyncConfigurationMain(syncConfiguration);
                                        insertRealm =  Realm.getInstance(syncConfiguration);
                                        insertRealm.executeTransaction(new Realm.Transaction() {
                                            @Override
                                            public void execute(Realm realm) {
                                                realm.insert(user);
                                            }
                                        });
                                        Log.d("CALAIDAPP-SignUp", String.valueOf(calAidApp.getSyncConfigurationMain()));
                                        addUserToSharedPreferences(user);
                                        Intent startAccServiceIntent = new Intent(getActivity().getApplicationContext(), AccelerometerService.class);
                                        startAccServiceIntent.setAction("startAccService");
                                        getActivity().getApplicationContext().startService(startAccServiceIntent);
                                        Intent startStepServiceIntent = new Intent(getActivity().getApplicationContext(), StepService.class);
                                        startStepServiceIntent.setAction("startStepService");
                                        getActivity().getApplicationContext().getApplicationContext().startService(startStepServiceIntent);
                                        Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                                        startActivity(mainIntent);
                                        loadingDialog.dismiss();
                                    } else {
                                        errorDialog = new ErrorDialog("Could not create an account.");
                                        Log.d(TAG, resultLogin.getError().toString());
                                    }
                                }
                            });
                        } else {
                            errorDialog = new ErrorDialog("Could not create an account.");
                            Log.d(TAG, resultRegister.getError().toString());
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    @Override
    public void update(SyncConfiguration syncConfiguration, io.realm.mongodb.User user) {
        this.syncConfiguration = syncConfiguration;
        this.mongoUser = user;
    }
}
