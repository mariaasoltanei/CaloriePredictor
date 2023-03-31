package ro.android.thesis;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.User;
import io.realm.mongodb.sync.ClientResetRequiredError;
import io.realm.mongodb.sync.DiscardUnsyncedChangesStrategy;
import io.realm.mongodb.sync.MutableSubscriptionSet;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;
import ro.android.thesis.domain.AccelerometerData;
import ro.android.thesis.services.AccelerometerService;
import ro.android.thesis.services.StepService;

public class CalAidApp extends Application {
    private static final String APP_ID = "caloriepredictor-rxpzo";
    private static SyncConfiguration syncConfigurationMain;
    int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private static User appUser;

    Realm realm;

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

//        syncConfigurationMain = new SyncConfiguration.Builder(app.currentUser())
//                .waitForInitialRemoteData()
//                .allowWritesOnUiThread(false)
//                .initialSubscriptions((realm, subscriptions) -> {
//                    subscriptions.remove("PasswordSubscription");
//                    subscriptions.add(Subscription.create("PasswordSubscription",
//                            realm.where(ro.android.thesis.domain.User.class)
//                                    .equalTo("password", "123456")));
//                    subscriptions.remove("AccelerometerData");
//                    subscriptions.add(Subscription.create("AccelerometerData",
//                            realm.where(ro.android.thesis.domain.AccelerometerData.class)
//                                    .equalTo("userId", CalAidApp.getApp().currentUser().getId())));
//                })
//                .build();
        Log.w("TIME", String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));



        //startForegroundService(new Intent(this, AccelerometerService.class));
        startForegroundService(new Intent(this, StepService.class));
    }

}