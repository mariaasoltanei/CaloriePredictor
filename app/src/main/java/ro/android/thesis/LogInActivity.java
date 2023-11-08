package ro.android.thesis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.dialogs.ErrorDialog;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.ActivityService;
import ro.android.thesis.services.GyroscopeService;
import ro.android.thesis.services.StepService;
import ro.android.thesis.utils.InputValidationUtils;
import ro.android.thesis.utils.KeyboardUtils;

public class LogInActivity extends AppCompatActivity implements AuthenticationObserver {
    Button btnLogin;
    Button btnRegister;

    LoadingDialog loadingDialog = new LoadingDialog();
    ErrorDialog errorDialog;
    EditText etLoginEmail;
    EditText etLoginPassword;
    User currentUser;
    User currentUserSharedPrefs;

    io.realm.mongodb.User mongoUser;
    SyncConfiguration syncConfiguration;
    private CalAidApp calAidApp;
    Realm getRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        calAidApp = (CalAidApp) getApplication();
        calAidApp.addObserver(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        KeyboardUtils.hideKeyboardOnClickOutside(this);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(i);
        });

        btnLogin = findViewById(R.id.btnLogIn);
        btnLogin.setOnClickListener(view -> {
            loginUser(etLoginEmail.getText().toString(), etLoginPassword.getText().toString());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        calAidApp.removeObserver(this);
    }

    private void addUserToSharedPreferences(User user) {
        SharedPreferences sharedPref = this.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        editor.putString("user", jsonUser);
        editor.apply();
    }

    public void loginUser(String email, String password){
        loadingDialog.setCancelable(false);
        loadingDialog.show(getSupportFragmentManager(), "loading_screen");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
//                if(InputValidationUtils.isValidEmail(etSignupEmail.getText().toString()) && InputValidationUtils.isValidPassword(etSignupPassword.getText().toString())){
//
//                }
                Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
                CalAidApp.getApp().loginAsync(emailPasswordCredentials, new App.Callback<io.realm.mongodb.User>() {
                    @Override
                    public void onResult(App.Result<io.realm.mongodb.User> resultLogin) {
                        if(resultLogin.isSuccess()){
                            mongoUser = CalAidApp.getApp().currentUser();
                            calAidApp.setAppUser(mongoUser);
                            syncConfiguration = new SyncConfiguration.Builder(mongoUser)
                                    .waitForInitialRemoteData()
                                    .allowWritesOnUiThread(false)
                                    .initialSubscriptions((realm, subscriptions) -> {
                                        subscriptions.remove("PasswordSubscription");
                                        subscriptions.add(Subscription.create("PasswordSubscription",
                                                realm.where(ro.android.thesis.domain.User.class)
                                                        .equalTo("password", password)));
                                        subscriptions.remove("AccelerometerData");
                                        subscriptions.add(Subscription.create("AccelerometerData",
                                                realm.where(ro.android.thesis.domain.AccelerometerData.class)
                                                        .equalTo("userId", calAidApp.getAppUser().getId())));
                                        subscriptions.remove("StepCount");
                                        subscriptions.add(Subscription.create("StepCount",
                                                realm.where(ro.android.thesis.domain.StepCount.class)
                                                        .equalTo("userId", calAidApp.getAppUser().getId())));
                                        subscriptions.remove("GyroscopeData");
                                        subscriptions.add(Subscription.create("GyroscopeData",
                                                realm.where(ro.android.thesis.domain.GyroscopeData.class)
                                                        .equalTo("userId", calAidApp.getAppUser().getId())));
                                    })
                                    .build();
                            calAidApp.setSyncConfigurationMain(syncConfiguration);
                            Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
                            Log.d("CALAIDAPP", String.valueOf(calAidApp.getSyncConfigurationMain()));
                            getRealm =  Realm.getInstance(syncConfiguration);
                            getRealm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    Log.d("CALAIDAPP", "AJUNGE LA EXECUTIE");
                                    RealmQuery<User> query = realm.where(User.class).equalTo("email", email).equalTo("password", password);
                                    currentUser = query.findFirst();
                                    Log.d("CALAIDAPP", currentUser.toString());
                                    if(currentUser != null){
                                        currentUserSharedPrefs = new User(currentUser.getId(),
                                                currentUser.getFirstName(),
                                                currentUser.getEmail(),
                                                currentUser.getPassword(),
                                                currentUser.getBirthDate(),
                                                currentUser.getHeight(),
                                                currentUser.getWeight(),
                                                currentUser.getGender(),
                                                currentUser.getActivityMultiplier());
                                        addUserToSharedPreferences(currentUserSharedPrefs);
                                        Log.d("SharedPrefs", currentUser.toString());
                                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(mainIntent);

                                    }
                                    else {
                                        Log.e("Realm", "LoginActivity/No user found");
                                    }
                                }
                            });
                            Intent startAccServiceIntent = new Intent(getApplicationContext(), AccelerometerService.class);
                            startAccServiceIntent.setAction("startAccService");
                            startService(startAccServiceIntent);
                            Intent startGyroServiceIntent = new Intent(getApplicationContext(), GyroscopeService.class);
                            startGyroServiceIntent.setAction("startGyroService");
                            startService(startGyroServiceIntent);
                            Intent startStepServiceIntent = new Intent(getApplicationContext(), StepService.class);
                            startStepServiceIntent.setAction("startStepService");
                            startService(startStepServiceIntent);

                            Intent activityServiceIntent = new Intent(getApplicationContext(), ActivityService.class);
                            activityServiceIntent.setAction("startActivityService");
                            startService(activityServiceIntent);
                            loadingDialog.dismiss();
                            getRealm.close();
                            //getApplicationContext().startService(new Intent(getApplicationContext(), AccelerometerService.class));
                            //getApplicationContext().startService(new Intent(getApplicationContext(), StepService.class));
                        }
                        else{
                            loadingDialog.dismiss();
                            calAidApp.setAppUser(null);
                            calAidApp.setSyncConfigurationMain(null);
                            Log.e("LoginActivity", resultLogin.getError().toString());
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
