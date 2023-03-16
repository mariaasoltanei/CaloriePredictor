//package ro.android.thesis;
//
//import android.content.Context;
//import android.util.Log;
//
//import io.realm.Realm;
//import io.realm.mongodb.App;
//import io.realm.mongodb.AppConfiguration;
//import io.realm.mongodb.sync.ClientResetRequiredError;
//import io.realm.mongodb.sync.DiscardUnsyncedChangesStrategy;
//import io.realm.mongodb.sync.SyncSession;
//
//public class RealmInit {
//    private static final String APP_ID = "caloriepredictor-rxpzo";
//    App app;
//    public static volatile  RealmInit INSTANCE;
//    private RealmInit(Context context){
//        try {
//            Realm.init(context);
//            Log.d("Realm", "Initialization succeeded");
//        } catch (Exception e) {
//            Log.e("Realm", "Initialization failed: " + e.getMessage());
//        }
//        app = new App(new AppConfiguration.Builder(APP_ID)
//                .defaultSyncErrorHandler((session, error) -> Log.e("RealmSync", error.toString()))
//                .defaultSyncClientResetStrategy(new DiscardUnsyncedChangesStrategy() {
//                    @Override
//                    public void onBeforeReset(Realm realm) {
//                        Log.w("EXAMPLE", "Beginning client reset for " + realm.getPath());
//                    }
//
//                    @Override
//                    public void onAfterReset(Realm before, Realm after) {
//                        Log.w("EXAMPLE", "Finished client reset for " + before.getPath());
//                    }
//
//                    @Override
//                    public void onError(SyncSession session, ClientResetRequiredError error) {
//                        Log.e("EXAMPLE", "Couldn't handle the client reset automatically." +
//                                " Falling back to manual recovery: " + error.getErrorMessage());
//                        //handleManualReset(session.getUser().getApp(), session, error);
//                        try {
//                            Log.w("EXAMPLE", "About to execute the client reset.");
//                            // execute the client reset, moving the current realm to a backup file
//                            error.executeClientReset();
//                            Log.w("EXAMPLE", "Executed the client reset.");
//                        } catch (IllegalStateException e) {
//                            Log.e("EXAMPLE", "Failed to execute the client reset: " + e.getMessage());
//
//                        }
//                    }
//                })
//                .build());
//    }
//
//    public static RealmInit getInstance(Context context){
//        if(INSTANCE == null){
//            synchronized (RealmInit.class){
//                if(INSTANCE == null){
//                    INSTANCE = new RealmInit(context);
//                }
//            }
//        }
//        return INSTANCE;
//    }
//    public App getApp() {
//        return app;
//    }
//}
