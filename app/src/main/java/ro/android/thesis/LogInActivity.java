package ro.android.thesis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.domain.User;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.StepService;
import ro.android.thesis.utils.KeyboardUtils;

public class LogInActivity extends AppCompatActivity implements AuthenticationObserver {
    Button btnLogin;
    Button btnRegister;

    LoadingDialog loadingDialog = new LoadingDialog();
    EditText etLoginEmail;
    EditText etLoginPassword;
    User currentUser;
    User currentUserSharedPrefs;
    io.realm.mongodb.User mongoUser;
    SyncConfiguration syncConfiguration;
    private CalAidApp calAidApp;

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
            loadingDialog.setCancelable(false);
            loadingDialog.show(getSupportFragmentManager(), "loading_screen");
            Credentials emailPasswordCredentials = Credentials.emailPassword(etLoginEmail.getText().toString(), etLoginPassword.getText().toString());
            CalAidApp.getApp().loginAsync(emailPasswordCredentials, it -> {
                if (it.isSuccess()) {
                    mongoUser = CalAidApp.getApp().currentUser();
                    calAidApp.setAppUser(mongoUser);
                    syncConfiguration = new SyncConfiguration.Builder(mongoUser)
                            .waitForInitialRemoteData()
                            .allowWritesOnUiThread(false)
                            .initialSubscriptions((realm, subscriptions) -> {
                                subscriptions.remove("PasswordSubscription");
                                subscriptions.add(Subscription.create("PasswordSubscription",
                                        realm.where(ro.android.thesis.domain.User.class)
                                                .equalTo("password", "123456")));
                                subscriptions.remove("AccelerometerData");
                                subscriptions.add(Subscription.create("AccelerometerData",
                                        realm.where(ro.android.thesis.domain.AccelerometerData.class)
                                                .equalTo("userId", CalAidApp.getApp().currentUser().getId())));
                                subscriptions.remove("StepCount");
                                subscriptions.add(Subscription.create("StepCount",
                                        realm.where(ro.android.thesis.domain.StepCount.class)
                                                .equalTo("userId", CalAidApp.getApp().currentUser().getId())));
                            })
                            .build();
                    calAidApp.setSyncConfigurationMain(syncConfiguration);
                    Log.d("CALAIDAPP", String.valueOf(calAidApp.getAppUser()));
                    Realm.getInstanceAsync(syncConfiguration, new Realm.Callback() {
                        @Override
                        public void onSuccess(Realm realm) {
                            Log.v(
                                    "Realm",
                                    "LoginActivity/Successfully opened a realm. UI THREAD"
                            );
                            RealmQuery<User> query = realm.where(User.class).equalTo("email", etLoginEmail.getText().toString()).equalTo("password", etLoginPassword.getText().toString());
                            currentUser = query.findFirst();
                            if (currentUser == null) {
                                Log.d("Realm", "LoginActivity/No user found");
                                //Todo: Add login errors
                            } else {
                                Log.d("Realm", "LoginActivity/" + currentUser.getId() + currentUser.getClass() + currentUser.getFirstName());
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
                                getApplicationContext().startService(new Intent(getApplicationContext(), AccelerometerService.class));
                                getApplicationContext().startService(new Intent(getApplicationContext(), StepService.class));
                                realm.close();
                                loadingDialog.dismiss();

                                final Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(mainIntent);
                            }

                        }
                    });

                } else {
                    Log.e("Realm", it.getError().toString());
                }
            });


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

    @Override
    public void update(SyncConfiguration syncConfiguration, io.realm.mongodb.User user) {
        this.syncConfiguration = syncConfiguration;
        this.mongoUser = user;
    }
}
