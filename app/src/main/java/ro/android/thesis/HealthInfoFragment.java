package ro.android.thesis;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

public class HealthInfoFragment extends Fragment{
    Button btnSignUp;
    HealthData healthData;
    EditText etSignUpHeight;
    EditText etSignUpWeight;
    EditText etSignUpGender;
    public HealthInfoFragment() {
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
        etSignUpGender = rootView.findViewById(R.id.etSignupGender);
        btnSignUp = rootView.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HealthInformation healthInformation = new HealthInformation(Integer.parseInt(etSignUpHeight.getText().toString()), Double.parseDouble(etSignUpWeight.getText().toString()), etSignUpGender.getText().toString());
                User user = createUser(name, email, password, birthDate, healthInformation);
                Log.d("USER", String.valueOf(user));
                Intent i = new Intent(getActivity(),MainActivity.class);
                startActivity(i);
            }
        });
        return rootView;
    }
    public HealthInformation createHeathInformation(int height, double weight, String gender){
        return new HealthInformation(height, weight, gender);
    }

    public User createUser(String firstName, String email, String password, String birthDate, HealthInformation healthInformation){
        return new User(firstName, email, password, birthDate, healthInformation);
    }

/*        void addUserToSharedPreferences(User user){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String userData = gson.toJson(user);
            editor.putString("User", userData);
        //editor.clear();
            editor.commit();
    }*/
}
