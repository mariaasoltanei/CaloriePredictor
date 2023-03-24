package ro.android.thesis;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.MutableSubscriptionSet;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;

public class CalAidApp extends Application {
    private static final String APP_ID = "caloriepredictor-rxpzo";
    private static SyncConfiguration syncConfigurationMain;

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

    public static SyncConfiguration getSyncConfigurationMain() {
        return syncConfigurationMain;
    }

    public static void setSyncConfigurationMain(SyncConfiguration syncConfigurationMain) {
        CalAidApp.syncConfigurationMain = syncConfigurationMain;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        app = new App(new AppConfiguration.Builder(APP_ID)
                .defaultSyncErrorHandler((session, error) -> Log.e("Realm", error.toString()))
                .build());
        syncConfigurationMain = new SyncConfiguration.Builder(app.currentUser())
                .waitForInitialRemoteData()
                .allowWritesOnUiThread(false)
                .initialSubscriptions((realm, subscriptions) -> {
                    subscriptions.remove("PasswordSubscription");
                    subscriptions.add(Subscription.create("PasswordSubscription",
                            realm.where(ro.android.thesis.domain.User.class)
                                    .equalTo("password", "123456")));
                })
                .build();
    }

}