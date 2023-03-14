
package ro.android.thesis;

import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;

public class RealmConnection {
    private static final String APP_ID = "caloriepredictor-rxpzo";
    //private static final String PARTITION = "your-partition";

    public static App connect() {
        App app = new App(new AppConfiguration.Builder(APP_ID)
                .build());
        return app;
    }

    public static void main(String[] args) {
        App app = connect();
        System.out.println("Connected to MongoDB Realm app: " + app.getConfiguration().getAppName());
    }
}

