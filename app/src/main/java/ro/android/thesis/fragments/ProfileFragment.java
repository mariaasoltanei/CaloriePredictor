package ro.android.thesis.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.bson.types.ObjectId;

import java.lang.reflect.Type;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import ro.android.thesis.CalAidApp;
import ro.android.thesis.R;
import ro.android.thesis.domain.User;

public class ProfileFragment extends Fragment {
    Realm realm;
    EditText etUpdateEmail;
    EditText etUpdateHeight;
    EditText etUpdateWeight;

    Button btnSaveUpdate;
    Button btnUpdatePassword;
    User user;
    Handler handler = new Handler(Looper.getMainLooper());
    User userCopy;

    public ProfileFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                realm = Realm.getInstance(CalAidApp.getSyncConfigurationMain());
                final User user  = realm.where(User.class).equalTo("_id", getObjectID()).findFirst();
                userCopy = realm.copyFromRealm(user);
                Log.d("Realm", "ProfileFragment/" + userCopy);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        etUpdateEmail.setText(userCopy.getEmail());
                        etUpdateHeight.setText(String.valueOf((int) userCopy.getHeight()));
                        etUpdateWeight.setText(String.valueOf((int) userCopy.getWeight()));
                    }
                });
                realm.close();
            }
        }).start();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        etUpdateEmail = rootView.findViewById(R.id.etUpdateEmail);
        etUpdateHeight = rootView.findViewById(R.id.etUpdateHeight);
        etUpdateWeight = rootView.findViewById(R.id.etUpdateWeight);
        btnSaveUpdate = rootView.findViewById(R.id.btnSaveUpdate);
        btnUpdatePassword = rootView.findViewById(R.id.btnUpdatePassword);

        btnSaveUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        realm = Realm.getInstance(CalAidApp.getSyncConfigurationMain());
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                userCopy.setFirstName(etUpdateEmail.getText().toString());
                                userCopy.setHeight(Integer.parseInt(etUpdateHeight.getText().toString()));
                                userCopy.setWeight(Integer.parseInt(etUpdateWeight.getText().toString()));
                                realm.insertOrUpdate(userCopy);
                            }
                        });
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

    private ObjectId getObjectID() {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(userLogged, type);
        return user.getId();
    }
}