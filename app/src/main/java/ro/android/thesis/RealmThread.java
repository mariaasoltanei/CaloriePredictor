package ro.android.thesis;

import android.content.Context;
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

public class RealmThread implements Runnable{
        io.realm.mongodb.User user;
        public RealmThread(io.realm.mongodb.User user) {
            this.user = user;
        }
        @Override
        public void run() {
            Realm realm = CalAidApp.getInstance().getCurrentRealm();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Car car = new Car();
                    car.setId(new ObjectId());
                    car.setName("Maria");
                    car.setAge("20");
                    realm.insert(car);
                }
            });
            realm.close();
        }
    }


