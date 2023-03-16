package ro.android.thesis;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;

public class CalAidApp extends Application {
    private static final String APP_ID = "caloriepredictor-rxpzo";
    private static final String API_KEY = "nJpGv1efocEzWAmkOSRpkcQWywGCWEyLKNJJRK9XtwzOd5aiSWwcYicb305vypDw";
    App app;
    SyncConfiguration syncConfiguration;
    private static CalAidApp instance;
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Realm.init(this);
            instance = this;
            app = new App(new AppConfiguration.Builder(APP_ID)
                    .defaultSyncErrorHandler((session, error) -> Log.e("MyApplication", error.toString()))
                    .build());
            Log.d("Realm", "Initialization succeeded APP");
            Credentials apiKeyCredentials = Credentials.apiKey(API_KEY);
            if (app.currentUser() == null) {
                app.login(apiKeyCredentials);
            }
            Log.d("Realm", app.currentUser().getId());
            syncConfiguration = new SyncConfiguration.Builder(app.currentUser())
                    //.initialSubscriptions(handler)
                    .waitForInitialRemoteData()
                    .compactOnLaunch()
                    .maxNumberOfActiveVersions(1)
                    .build();
            SyncSession session = app.getSync().getSession(syncConfiguration);
        } catch (Exception e) {
            Log.e("Realm", "Initialization failed: " + e.getMessage());
        }
    }
    public static CalAidApp getInstance() {
        return instance;
    }

    public Realm getCurrentRealm(){
        return Realm.getInstance(syncConfiguration);
    }

    public App getApp() {
        return app;
    }

    public SyncConfiguration getSyncConfiguration() throws InterruptedException {
        app.getSync().getSession(syncConfiguration).downloadAllServerChanges();
        app.getSync().getSession(syncConfiguration).uploadAllLocalChanges();
        return syncConfiguration;
    }
}