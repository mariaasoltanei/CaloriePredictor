package ro.android.thesis;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;

public class CalAidApp extends Application {
    private static final String APP_ID = "caloriepredictor-rxpzo";

    private static User appUser;


    private static App app;

    public static App getApp() {
        return app;
    }

    public static User getCurrentUser() {
        return appUser;
    }

    public static void setCurrentUser(User user) {
        appUser = user;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(APP_ID)
                .defaultSyncErrorHandler((session, error) -> Log.e("Realm", error.toString()))
                .build());
    }

}