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
import ro.android.thesis.domain.User;

public class LogInActivity extends AppCompatActivity {
    Button btnLogin;
    Button btnRegister;

    EditText etLoginEmail;
    EditText etLoginPassword;
    User user;
    User userSharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        btnRegister = findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), SignUpActivity.class);
            startActivity(i);
        });

        btnLogin = findViewById(R.id.btnLogIn);

        btnLogin.setOnClickListener(view -> {
            Credentials emailPasswordCredentials = Credentials.emailPassword(etLoginEmail.getText().toString(), etLoginPassword.getText().toString());
            CalAidApp.getApp().loginAsync(emailPasswordCredentials, it -> {
                if (it.isSuccess()) {
                    String jwt = CalAidApp.getApp().currentUser().getAccessToken();
                    SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(CalAidApp.getApp().currentUser())
                            .waitForInitialRemoteData()
                            //.allowWritesOnUiThread(true)
                            .initialSubscriptions((realm, subscriptions) -> {
                                subscriptions.remove("PasswordSubscription");
                                subscriptions.add(Subscription.create("PasswordSubscription",
                                        realm.where(User.class)
                                                .equalTo("password", "123456")));
                            })
                            .build();
                    CalAidApp.setSyncConfigurationMain(syncConfiguration);
                    Realm.getInstanceAsync(syncConfiguration, new Realm.Callback() {
                        @Override
                        public void onSuccess(Realm realm) {
                            Log.v(
                                    "Realm",
                                    "LoginActivity/Successfully opened a realm. UI THREAD"
                            );
                            RealmQuery<User> query = realm.where(User.class).equalTo("email", etLoginEmail.getText().toString()).equalTo("password", etLoginPassword.getText().toString());
                            user = query.findFirst();
                            if (user == null) {
                                Log.d("Realm", "LoginActivity/No user found");
                                //Todo: Add login errors
                            } else {
                                Log.d("Realm", "LoginActivity/" + user.getId() + user.getClass() + user.getFirstName());
                                userSharedPrefs = new User(user.getId(), user.getFirstName(), user.getEmail(), user.getPassword(), user.getBirthDate(), user.getHeight(), user.getWeight(), user.getGender(), user.getActivityMultiplier());
                                addUserToSharedPreferences(userSharedPrefs);
                                realm.close();
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


    private void addUserToSharedPreferences(User user) {
        SharedPreferences sharedPref = this.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        editor.putString("user", jsonUser);
        //editor.clear();
        editor.apply();
    }

}
