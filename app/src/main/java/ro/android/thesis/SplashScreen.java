package ro.android.thesis;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class SplashScreen extends AppCompatActivity {
    ImageView imgSplashScreen;
    Animation animSideSlide;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        context = this;
        imgSplashScreen = findViewById(R.id.imgSplashScreen);
        animSideSlide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.side_slide);
        imgSplashScreen.startAnimation(animSideSlide);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isSharedPrefs = checkUserSharedPreferences();
                if (isSharedPrefs) {
                    final Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainIntent);
                } else {
                    final Intent mainIntent = new Intent(getApplicationContext(), LogInActivity.class);
                    startActivity(mainIntent);
                }
                //finish();
            }
        }, 2000);

    }

    private boolean checkUserSharedPreferences() {
        File f = new File(
                "/data/data/ro.android.thesis/shared_prefs/userDetails.xml");
        if (f.exists()) {
            SharedPreferences sharedPref = context.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
            if (sharedPref.getString("user", null) == null) {
                return false;
            }
        }
        return true;

    }

}
