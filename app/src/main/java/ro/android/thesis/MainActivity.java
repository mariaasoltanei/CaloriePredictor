package ro.android.thesis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import ro.android.thesis.domain.User;
import ro.android.thesis.services.AccelerometerService;


public class MainActivity extends AppCompatActivity {
    TextView navMenuName;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        //Realm.init(context);

        //Realm.init(context);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        findViewById(R.id.imgIconMenu).setOnClickListener(view -> drawerLayout.openDrawer(Gravity.LEFT));
        NavigationView navigationView = findViewById(R.id.navigationView);
        navMenuName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.navMenuName);
        navigationView.setItemIconTintList(null);

        NavController navController = Navigation.findNavController(this, R.id.navHostFragment);
        NavigationUI.setupWithNavController(navigationView, navController);
        navHeaderMenu();
        startForegroundService(new Intent(this, AccelerometerService.class));

    }

    private void navHeaderMenu() {
        SharedPreferences sharedPref = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        String userLogged = sharedPref.getString("user", null);
        Gson gson = new Gson();
        Type type = new TypeToken<User>() {
        }.getType();
        User user = gson.fromJson(userLogged, type);

        //Log.d("SHARED PREFS TEST", user.getBirthDate().substring(user.getBirthDate().length() - 4));
        if (user != null) {
            navMenuName.setText(user.getFirstName());
        }
    }

}
