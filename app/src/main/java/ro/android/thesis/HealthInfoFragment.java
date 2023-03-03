package ro.android.thesis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HealthInfoFragment extends Fragment{
    Button btnSignUp;
    EditText etSignUpHeight;
    EditText etSignUpWeight;
    Spinner spinSignUpGender;

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

        spinSignUpGender = rootView.findViewById(R.id.spinSignUpGender);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.genders_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinSignUpGender.setAdapter(spinnerAdapter);

        btnSignUp = rootView.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectGender();
                HealthInformation healthInformation = new HealthInformation(Integer.parseInt(etSignUpHeight.getText().toString()), Double.parseDouble(etSignUpWeight.getText().toString()), String.valueOf(spinSignUpGender.getSelectedItem()));
                User user = new User(name, email, password, birthDate, healthInformation);
                Log.d("USER", String.valueOf(user));
                addUserToSharedPreferences(user);
                Intent i = new Intent(getActivity(),MainActivity.class);
                startActivity(i);
            }
        });
        return rootView;
    }

    void addUserToSharedPreferences(User user) {
        SharedPreferences sharedPref = this.getContext().getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        Gson gson = new Gson();
        String jsonUser = gson.toJson(user);
        editor.putString("user", jsonUser);
        //editor.clear();
        editor.apply();
    }

    private void selectGender(){
        spinSignUpGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedGender = adapterView.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

}
