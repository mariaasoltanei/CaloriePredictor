package ro.android.thesis;

import android.util.Log;

import org.bson.types.ObjectId;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.sync.ClientResetRequiredError;
import io.realm.mongodb.sync.DiscardUnsyncedChangesStrategy;
import io.realm.mongodb.sync.MutableSubscriptionSet;
import io.realm.mongodb.sync.Subscription;
import io.realm.mongodb.sync.SyncConfiguration;
import io.realm.mongodb.sync.SyncSession;

public class RealmThread extends Thread{
    private static final String APP_ID = "caloriepredictor-rxpzo";
    private static final String API_KEY = "nJpGv1efocEzWAmkOSRpkcQWywGCWEyLKNJJRK9XtwzOd5aiSWwcYicb305vypDw";
    @Override
    public void run() {
        super.run();
        App app = new App(new AppConfiguration.Builder(APP_ID)
                .defaultSyncErrorHandler((session, error) -> Log.e("RealmSync", error.toString()))
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
                        Log.e("EXAMPLE", "Couldn't handle the client reset automatically." +
                                " Falling back to manual recovery: " + error.getErrorMessage());
                        //handleManualReset(session.getUser().getApp(), session, error);
                        try {
                            Log.w("EXAMPLE", "About to execute the client reset.");
                            // execute the client reset, moving the current realm to a backup file
                            error.executeClientReset();
                            Log.w("EXAMPLE", "Executed the client reset.");
                        } catch (IllegalStateException e) {
                            Log.e("EXAMPLE", "Failed to execute the client reset: " + e.getMessage());

                        }
                    }
                })
                .build());
        Credentials apiKeyCredentials = Credentials.apiKey(API_KEY);
        app.login(apiKeyCredentials);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(false)
                .allowQueriesOnUiThread(false)
                .name("calaid_android")
                .build();
        //Realm.setDefaultConfiguration(realmConfiguration);
        SyncConfiguration.InitialFlexibleSyncSubscriptions handler = new SyncConfiguration.InitialFlexibleSyncSubscriptions() {
            @Override
            public void configure(Realm realm, MutableSubscriptionSet subscriptions) {
                subscriptions.add(
                        Subscription.create(
                                realm.where(Car.class).equalTo("name", "Bob")
                        )
                );
            }
        };
        SyncConfiguration flexibleSyncConfig = new SyncConfiguration.Builder(app.currentUser())
                .initialSubscriptions(handler)
                //.allowWritesOnUiThread(true)
                .build();

        Realm realm = Realm.getInstance(flexibleSyncConfig);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Car car = new Car();
                car.setId(new ObjectId());
                car.setName("Bob");
                car.setAge("20");
                realm.insert(car);
        }
    });

        realm.close();
    }

}
