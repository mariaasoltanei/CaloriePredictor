package ro.android.thesis;

import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public interface AuthenticationObserver {
//    void onUserLoggedIn(SyncConfiguration syncConfiguration, User user);
//    void onUserSignedUp(SyncConfiguration syncConfiguration, User user);
//    void onUserLoggedOut(SyncConfiguration syncConfiguration, User user);
    void update(SyncConfiguration syncConfiguration, User user);
}

