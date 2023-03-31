package ro.android.thesis.fragments;

import static java.lang.Double.parseDouble;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import org.bson.types.ObjectId;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.MainActivity;
import ro.android.thesis.R;
import ro.android.thesis.dialogs.LoadingDialog;
import ro.android.thesis.domain.User;
import ro.android.thesis.utils.KeyboardUtils;

public class HealthInfoFragment extends Fragment {
    private static SyncConfiguration syncConfiguration;
    Button btnSignUp;
    EditText etSignUpHeight;
    EditText etSignUpWeight;
    Spinner spinSignUpGender;
    Spinner spinSignUpActivity;
    LoadingDialog loadingDialog = new LoadingDialog();

    public HealthInfoFragment() {
    }

    public static SyncConfiguration getSyncConfiguration() {
        return syncConfiguration;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        KeyboardUtils.setupUI(rootView, this.getActivity());
        btnSignUp = rootView.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(view -> {
            loadingDialog.setCancelable(false);
            loadingDialog.show(getChildFragmentManager(), "loading_screen");
            ObjectId objectId = new ObjectId();
            User user = new User(objectId, name, email, password, birthDate,
                    parseDouble(etSignUpHeight.getText().toString()),
                    Double.parseDouble(etSignUpWeight.getText().toString()),
                    String.valueOf(spinSignUpGender.getSelectedItem()),
                    selectActivityMultiplier(spinSignUpActivity.getSelectedItem().toString()));
            Log.d("USER", String.valueOf(user));

            CalAidApp.getApp().getEmailPassword().registerUserAsync(email, password, new App.Callback<Void>() {
                @Override
                public void onResult(App.Result<Void> result) {
                    Credentials emailPasswordCredentials = Credentials.emailPassword(email, password);
                    CalAidApp.getApp().loginAsync(emailPasswordCredentials, it -> {
                        if (it.isSuccess()) {
                            Log.v("Realm", "Successfully authenticated using an email and password.");

                            syncConfiguration = new SyncConfiguration.Builder(CalAidApp.getApp().currentUser())
                                    .waitForInitialRemoteData()
                                    .allowWritesOnUiThread(true)
                                    .initialSubscriptions((realm, subscriptions) -> {
                                        subscriptions.remove("PasswordSubscription");
                                        subscriptions.add(Subscription.create("PasswordSubscription",
                                                realm.where(User.class)
                                                        .equalTo("password", password)));
                                    })
                                    .build();
                            CalAidApp.setSyncConfigurationMain(syncConfiguration);
                            Realm.getInstanceAsync(CalAidApp.getSyncConfigurationMain(), new Realm.Callback() {
                                @Override
                                public void onSuccess(Realm realm) {
                                    Log.v(
                                            "Realm",
                                            "Successfully opened a realm with reads and writes allowed on the UI thread."
                                    );
                                    realm.executeTransaction(realm1 -> realm1.insert(user));
                                    //realm.executeTransaction(realm ->  realm);
                                    realm.close();
                                }
                            });
                            addUserToSharedPreferences(user);
                            CalAidApp.setCurrentUser(CalAidApp.getApp().currentUser());
                            loadingDialog.dismiss();
                            Intent i = new Intent(getActivity(), MainActivity.class);
                            startActivity(i);
                        } else {
                            Log.e("Realm", it.getError().toString());
                        }
                    });
                }
            });
        });
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //realm.close();
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

}
