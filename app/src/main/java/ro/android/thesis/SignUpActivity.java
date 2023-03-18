package ro.android.thesis;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import ro.android.thesis.fragments.PersonalInfoFragment;

public class SignUpActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

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
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

//    void addUserSharedPreferences(){
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        Gson gson = new Gson();
//        String jsonDiceRolls = gson.toJson(diceRolls);
//        editor.putString("diceRolls", jsonDiceRolls);
//        //editor.clear(); -> pt testing de empty history
//        editor.commit();
//    }
}
