package ro.android.thesis;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import ro.android.thesis.fragments.PersonalInfoFragment;

public class SignUpActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        PersonalInfoFragment personalInfoFragment = new PersonalInfoFragment();
        fragmentTransaction.add(R.id.signupHostFragment, personalInfoFragment);
        fragmentTransaction.commit();
    }

}
