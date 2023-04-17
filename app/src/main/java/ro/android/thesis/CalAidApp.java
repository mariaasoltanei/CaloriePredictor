package ro.android.thesis;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.ClientResetRequiredError;
import io.realm.mongodb.sync.DiscardUnsyncedChangesStrategy;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.StepService;

public class CalAidApp extends Application {
    private static final String APP_ID = "caloriepredictor-rxpzo";
    private static App app;
    private final List<AuthenticationObserver> observers = new ArrayList<>();
    private SyncConfiguration syncConfigurationMain;
    private User appUser;

    public boolean isAccServiceRunning;
    public boolean isStepServiceRunning;

    public static App getApp() {
        return app;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);

        app = new App(new AppConfiguration.Builder(APP_ID)
                .defaultSyncErrorHandler((session, error) -> Log.e("Realm", error.toString()))
                .defaultSyncClientResetStrategy(new DiscardUnsyncedChangesStrategy() {
                    @Override
                    public void onBeforeReset(Realm realm) {
                        Log.w("EXAMPLE", "Beginning client reset for " + realm.getPath());
                    }

                    @Override
                    public void onAfterReset(Realm before, Realm after) {
                        Log.w("EXAMPLE", "Finished client reset for " + before.getPath());
                    }

                    @Override
                    public void onError(SyncSession session, ClientResetRequiredError error) {
                        try {
                            Log.w("EXAMPLE", "About to execute the client reset.");
                            // execute the client reset, moving the current realm to a backup file
                            error.executeClientReset();
                            Log.w("EXAMPLE", "Executed the client reset.");
                        } catch (IllegalStateException e) {
                            Log.e("EXAMPLE", "Failed to execute the client reset: " + e.getMessage());
                            // The client reset can only proceed if there are no open realms.
                            // if execution failed, ask the user to restart the app, and we'll client reset
                            // when we first open the app connection.
                            AlertDialog restartDialog = new AlertDialog.Builder(getApplicationContext())
                                    .setMessage("Sync error. Restart the application to resume sync.")
                                    .setTitle("Restart to Continue")
                                    .create();
                            restartDialog.show();
                        }
                        // open a new instance of the realm. This initializes a new file for the new realm
                        // and downloads the backend state. Do this in a background thread so we can wait
                        // for server changes to fully download.
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        executor.execute(() -> {
                            Realm newRealm = Realm.getInstance(syncConfigurationMain);
                            // ensure that the backend state is fully downloaded before proceeding
                            try {
                                app.getSync().getSession(syncConfigurationMain).downloadAllServerChanges(10000,
                                        TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.w("EXAMPLE",
                                    "Downloaded server changes for a fresh instance of the realm.");
                            newRealm.close();
                        });
                        // execute the recovery logic on a background thread
                        try {
                            executor.awaitTermination(20000, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .build());

        Log.d("CALAIDAPP - Application", String.valueOf(app.currentUser()));
        Log.d("CALAIDAPP - Application", String.valueOf(syncConfigurationMain));

        if (app.currentUser() != null && syncConfigurationMain == null) {
            /**DEBUGGING*/
//            app.currentUser().logOutAsync(new App.Callback<User>() {
//                @Override
//                public void onResult(App.Result<User> result) {
//                    Log.d("CALAIDAPP - Application", String.valueOf(app.currentUser()));
//                }
//            });

            isAccServiceRunning = true;
            isStepServiceRunning = true;
        } else {
            isAccServiceRunning = false;
            isStepServiceRunning = false;
        }
    }

    public SyncConfiguration getSyncConfigurationMain() {
        return syncConfigurationMain;
    }

    public void setSyncConfigurationMain(SyncConfiguration syncConfigurationMain) {
        this.syncConfigurationMain = syncConfigurationMain;
        notifyAuthenticationObservers();
    }

    public User getAppUser() {
        return appUser;
    }

    public void setAppUser(User appUser) {
        this.appUser = appUser;
        notifyAuthenticationObservers();
    }

    public void addObserver(AuthenticationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AuthenticationObserver observer) {
        observers.remove(observer);
    }

    public void notifyAuthenticationObservers() {
        for (AuthenticationObserver observer : observers) {
            observer.update(syncConfigurationMain, appUser);
        }
    }
}