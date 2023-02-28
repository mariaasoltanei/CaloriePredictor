package ro.android.thesis;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

public class PersonalInfoFragment extends Fragment {
    Button btnNext;
    AccountData accountData;
    EditText etSignUpName;
    EditText etBirthDate;
    EditText etSignupEmail;
    EditText etSignupPassword;
    public PersonalInfoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //accountData = (AccountData) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personal_info, container, false);
        etSignUpName = rootView.findViewById(R.id.etSignupName);
        etBirthDate = rootView.findViewById(R.id.etBirthDate);
        etSignupEmail = rootView.findViewById(R.id.etSignupEmail);
        etSignupPassword = rootView.findViewById(R.id.etSignupPassword);
        btnNext = rootView.findViewById(R.id.btnNextHealthInfo);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.println(Log.DEBUG,"cliecked", "CLICKED");
                Bundle bundleAccountInfo = new Bundle();
                bundleAccountInfo.putString("name", etSignUpName.getText().toString());
                bundleAccountInfo.putString("email", etSignupEmail.getText().toString());
                bundleAccountInfo.putString("password", etSignupPassword.getText().toString());
                bundleAccountInfo.putString("birthDate", etBirthDate.getText().toString());

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                HealthInfoFragment healthInfoFragment = new HealthInfoFragment();
                healthInfoFragment.setArguments(bundleAccountInfo);
                fragmentTransaction.replace(R.id.signupHostFragment, healthInfoFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
        return rootView;
    }

}
