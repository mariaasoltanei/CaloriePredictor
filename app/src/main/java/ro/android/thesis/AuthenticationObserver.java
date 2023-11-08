package ro.android.thesis;

import io.realm.mongodb.User;
import io.realm.mongodb.sync.SyncConfiguration;

public interface AuthenticationObserver {
    void update(SyncConfiguration syncConfiguration, User user);
}

