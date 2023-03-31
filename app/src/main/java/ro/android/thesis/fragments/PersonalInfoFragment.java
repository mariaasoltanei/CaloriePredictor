package ro.android.thesis.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Calendar;

import ro.android.thesis.R;
import ro.android.thesis.fragments.HealthInfoFragment;
import ro.android.thesis.utils.KeyboardUtils;

public class PersonalInfoFragment extends Fragment {
    Button btnNext;

    EditText etSignUpName;
    EditText etSignupEmail;
    EditText etSignupPassword;

    private DatePickerDialog datePickerDialog;
    private Button btnBirthDatePicker;

    public PersonalInfoFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.personal_info, container, false);
        initDatePicker();
        etSignUpName = rootView.findViewById(R.id.etSignupName);
        etSignupEmail = rootView.findViewById(R.id.etSignupEmail);
        etSignupPassword = rootView.findViewById(R.id.etSignupPassword);
        KeyboardUtils.setupUI(rootView, this.getActivity());
        btnBirthDatePicker = rootView.findViewById(R.id.btnBirthDatePicker);
        btnBirthDatePicker.setText(getTodaysDate());
        btnBirthDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePicker(rootView);
            }
        });

        btnNext = rootView.findViewById(R.id.btnNextHealthInfo);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundleAccountInfo = new Bundle();
                bundleAccountInfo.putString("name", etSignUpName.getText().toString());
                bundleAccountInfo.putString("email", etSignupEmail.getText().toString());
                bundleAccountInfo.putString("password", etSignupPassword.getText().toString());
                bundleAccountInfo.putString("birthDate", btnBirthDatePicker.getText().toString());

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
    private String getTodaysDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return makeDateString(day, month, year);
    }
    private String getMonthFormat(int month)
    {
        if(month == 1)
            return "JAN";
        if(month == 2)
            return "FEB";
        if(month == 3)
            return "MAR";
        if(month == 4)
            return "APR";
        if(month == 5)
            return "MAY";
        if(month == 6)
            return "JUN";
        if(month == 7)
            return "JUL";
        if(month == 8)
            return "AUG";
        if(month == 9)
            return "SEP";
        if(month == 10)
            return "OCT";
        if(month == 11)
            return "NOV";
        if(month == 12)
            return "DEC";

        return "JAN";
    }

    public void openDatePicker(View view)
    {
        datePickerDialog.show();
    }
    private String makeDateString(int day, int month, int year)
    {
        return day + "-" + getMonthFormat(month) + "-" + year;
    }

    private void initDatePicker()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String birthDate = makeDateString(day, month, year);
            btnBirthDatePicker.setText(birthDate);
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this.getContext(), style, dateSetListener, year, month, day);
    }


}
